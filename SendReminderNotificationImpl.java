package com.telstra.eomsys.oss.sfo.logistics.inbound.kakfa.activity.common;

import com.amdocs.oss.aff.schema.executionplan.Activity;
import com.amdocs.oss.aff.schema.executionplan.ExecutionPlan;
import com.amdocs.oss.aff.schema.project.Project;
import com.amdocs.oss.xml.order.v1.OrderItemValue;
import com.amdocs.oss.xml.order.v1.OrderLineValue;
import com.amdocs.oss.xml.order.v1.OrderValue;
import com.telstra.eomsys.oss.sfo.common.adt.domain.CreateDomainBeanContext;
import com.telstra.eomsys.oss.sfo.common.core.exception.TelstraException;
import com.telstra.eomsys.oss.sfo.common.core.exception.constants.ErrorCodes;
import com.telstra.eomsys.oss.sfo.common.domain.annotation.Create;
import com.telstra.eomsys.oss.sfo.common.domain.annotation.DomainBean;
import com.telstra.eomsys.oss.sfo.common.domain.annotation.Generated;
import com.telstra.eomsys.oss.sfo.common.domain.constant.TLSConstants;
import com.telstra.eomsys.oss.sfo.common.domain.constant.TLSNotificationConstants;
import com.telstra.eomsys.oss.sfo.common.domain.model.ExecutionParam;
import com.telstra.eomsys.oss.sfo.common.domain.model.ExternalNotificationVO;
import com.telstra.eomsys.oss.sfo.common.domain.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.telstra.eomsys.oss.sfo.common.domain.constant.TLSConstants.AVOS.EVENT_OPER;
import static com.telstra.eomsys.oss.sfo.common.domain.constant.TLSConstants.AVOS.QUEUE_EOMSYS_GENERIC_SEND_NOTIFICATION;
import static com.telstra.eomsys.oss.sfo.common.domain.constant.TLSConstants.ActivityName.MLO_REMEDIATION_LOGISTICS_STATUS;
import static com.telstra.eomsys.oss.sfo.common.domain.constant.TLSConstants.Attribute.*;
import static com.telstra.eomsys.oss.sfo.common.domain.constant.TLSConstants.MilestoneID.TRACK_STOCK_ALLOCATED_EVENT;
import static com.telstra.eomsys.oss.sfo.common.domain.constant.TLSConstants.NotificationType.STOCK_NOT_ALLOCATED_NOTIFICATION;
import static com.telstra.eomsys.oss.sfo.common.domain.constant.TLSConstants.OrderItemStatus.*;
import static com.telstra.eomsys.oss.sfo.common.domain.constant.TLSNotificationConstants.NotificationConstants.*;

/**
 * Send reminder notification for Stock Not Allocated.
 */
@DomainBean(
    domain = "logistics",
    name = "SendReminderNotificationImpl"
)
public class SendReminderNotificationImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendReminderNotificationImpl.class);
    private static final String NOTIFICATION_DAYS = "Stock is not allocated after %s days";
    private static final String PRODUCT_ID_VOICE = "productId";
    private static final String ORDER_ITEM_ACTION_VOICE = "orderItemAction";

    @Autowired
    ActivityImplementationUtil activityImplementationUtil;
    @Autowired
    DPMUtil dpmUtil;
    @Autowired
    IntegrationServicesUtil integrationServicesUtil;
    @Autowired
    CorrelationUtil correlationUtil;
    @Autowired
    private NotificationUtil notificationUtil;

    @Generated(message = "")
    @Create
    protected void createImpl(CreateDomainBeanContext activityContext) throws TelstraException {
        LOGGER.info("Implementation of Send Reminder Notification for Stock Allocated");

        if (null == activityContext) {
            throw new TelstraException(ErrorCodes.EMPTY_ACTIVITY_CONTEXT.getErrorCode().getCode(), new Exception("Activity Context cannot be null"));
        }
        String activityID = null;
        String planId = null;
        String projectId = null;
        try {
            activityID = activityContext.getActivityInstanceKey().getActivityInstanceID();
            planId = activityContext.getActivityInstanceKey().getExecutionPlanID();
            projectId = activityContext.getProjectID();
            Map<String, String> mapProjectStore = activityImplementationUtil.getProjectStoreAttributesMap(projectId);

            String orderItemStatus = mapProjectStore.get(ORDER_ITEM_STATUS);
            if(!CommonMethods.isEmpty(orderItemStatus)) { //CHECK QUERY-MLO SUCCESS
                List<String> allowedStatus = Arrays.asList(STOCK_ALLOCATED, DISPATCHED, IN_TRANSIT, DELIVERED, COMPLETED, DELIVERED, TLSConstants.OrderItemStatus.DELIVERY_FAILED);
                if (allowedStatus.contains(orderItemStatus.toLowerCase())) {
                    //ACHIEVE MILESTONE
                    dpmUtil.achieveMilestone(projectId, TRACK_STOCK_ALLOCATED_EVENT);
                    LOGGER.info(TRACK_STOCK_ALLOCATED_EVENT + " Milestone Achieved");
                } else {
                    //SEND NOTIFICATION
                    String rootOrderId = mapProjectStore.get(ROOT_ORDERID);

                    String correlationId = null;
                    OrderValue orderValue = integrationServicesUtil.getContextStore(rootOrderId);
                    correlationId = integrationServicesUtil.getCorrelationId(rootOrderId);

                    ExternalNotificationVO externalNotificationVO = new ExternalNotificationVO(projectId, planId, activityID,
                            TLSNotificationConstants.NotificationConstants.ALL, TLSNotificationConstants.NotificationConstants.CLOUDSENSE_VOICE,
                            TLSNotificationConstants.NotificationConstants.SENDNOTIFICATION, TLSNotificationConstants.NotificationConstants.CREATE_VOICE);

                    Set<OrderItemValue> orderItemValues = findAssociatedOrderItems(orderValue);

                    List<ExecutionParam> initialParamList = getExecutionParamList(mapProjectStore, correlationId, orderValue);
                    for (OrderItemValue orderItemValue : orderItemValues) {
                        List<ExecutionParam> finalParamList = getExecutionParamListFromOrder(mapProjectStore, initialParamList, orderItemValue);
                        notificationUtil.sendExternalNotification(externalNotificationVO, finalParamList);
                        LOGGER.info("Notification Sent");
                    }
                }
                completeOpenDetachedAdhocActivity(projectId, TLSConstants.ActivityName.MLO_REMEDIATION_LOGISTICS_STATUS_SPEC_ID);
                resetOrderItemStatus(projectId);
            }
            dpmUtil.updateActivityStatus(planId, activityID, TLSConstants.ActivityState.COMPLETED);
        } catch (Exception e) {
            integrationServicesUtil.reportErrorAndLogEvent(LOGGER, e, planId, activityID, "SendMobileBoltOnServiceModifyCompletionNotification");
            throw new TelstraException(e.getMessage(), e);
        }
    }

    private void resetOrderItemStatus(String projectId) throws Exception {
        integrationServicesUtil.updateProjectStore(projectId, ORDER_ITEM_STATUS, null);
    }

    List<ExecutionParam> getExecutionParamListFromOrder(Map<String, String> mapProjectStore, List<ExecutionParam> initialParamList, OrderItemValue orderItemValue) {
        List<ExecutionParam> executionParamList = new ArrayList<>();
        String orderItemId = orderItemValue.getId();
        String productSerial = mapProjectStore.get("OrderItem[" + orderItemId + "].productSerial");
        executionParamList.addAll(initialParamList);
        executionParamList.add(new ExecutionParam(ORDER_ITEM_ID, orderItemId));
        executionParamList.add(new ExecutionParam(PRODUCT_ID_VOICE, orderItemValue.getRootOrderLine().getEntity().getId()));
        executionParamList.add(new ExecutionParam(PRODUCT_CODE, orderItemValue.getRootOrderLine().getEntity().getDescribingSpecificationKey().getPrimaryKey()));
        executionParamList.add(new ExecutionParam(PRODUCT_ACTION_VOICE, getProductActionType(orderItemValue)));
        executionParamList.add(new ExecutionParam(ORDER_ITEM_ACTION_VOICE, orderItemValue.getActionType()));
        executionParamList.add(new ExecutionParam(ORDER_ITEM_STATUS_VOICE,  IN_PROGRESS));
        executionParamList.add(new ExecutionParam(VERSION_VOICE, orderItemValue.getVersion().toString()));
        executionParamList.add(new ExecutionParam(PRODUCT_SERIAL, productSerial));
        executionParamList.add(new ExecutionParam(SERIAL_NUMBER, productSerial));
        return executionParamList;
    }

    private List<ExecutionParam> getExecutionParamList(Map<String, String> mapProjectStore, String correlationId, OrderValue orderValue) {
        List<ExecutionParam> paramList = new ArrayList<>();
        String esclationDays = mapProjectStore.get(ESCALATION_DAYS);
        String bookingId = mapProjectStore.get(LOGISTICS_BOOKING_ID);
        if(correlationId != null) {
            paramList.add(new ExecutionParam(TLSConstants.DataAttribute.CORRELATIONID, correlationId));
        }
        paramList.add(new ExecutionParam(EXTERNAL_ORDER_ID, mapProjectStore.get(ORDER_EXTERNAL_ID)));
        paramList.add(new ExecutionParam(STATUS_VOICE, IN_PROGRESS));
        paramList.add(new ExecutionParam(NOTIFICATION_TYPE, STOCK_NOT_ALLOCATED_NOTIFICATION));
        paramList.add(new ExecutionParam(SUB_NOTIFCATION_TYPE, String.format(NOTIFICATION_DAYS, esclationDays)));
        paramList.add(new ExecutionParam(CUSTOMER_ACCOUNT_UUID, orderValue.getCustomer().getId()));
        paramList.add(new ExecutionParam(BOOKING_ID, bookingId));
        paramList.add(new ExecutionParam(EVENT_OPER, QUEUE_EOMSYS_GENERIC_SEND_NOTIFICATION));
        paramList.add(new ExecutionParam(STATUS_OUTCOME, "Info"));
        paramList.add(new ExecutionParam(ORDERID, mapProjectStore.get(ORDER_ID)));
        paramList.add(new ExecutionParam(ROOT_ORDER_ID, mapProjectStore.get(ROOT_ORDERID)));
        return paramList;
    }

    @Generated(message = "")
    public void completeOpenDetachedAdhocActivity(String projectId, String activitySpecId) throws Exception {
        Project projectOrder = integrationServicesUtil.getProjectById(projectId);
            String parentPlanId = projectOrder.getPlanID();
            ExecutionPlan adhocPlan = dpmUtil.getExecutionPlanViewByPlanId(parentPlanId);
            for (Activity activity : adhocPlan.getActivities().getActivity()) {
                if (activity.getActivitySpec().getSpecID().equals(activitySpecId)) {
                    com.amdocs.oss.aff.schema.executionplanview.Activity activityInstanceDTO =  activityImplementationUtil.getActivityDetails(parentPlanId, activity.getID());
                    if(!TLSConstants.ActivityState.COMPLETED.equals(activityInstanceDTO.getState())) {
                        DPMUtil.getInstance().updateActivityStatus(parentPlanId, activity.getID(), TLSConstants.ActivityState.COMPLETED);
                        LOGGER.info(MLO_REMEDIATION_LOGISTICS_STATUS + " Adhoc completed");
                    }
                }
            }
    }

    private Set<OrderItemValue> findAssociatedOrderItems(OrderValue orderValue) {
        String[] productItemType = { TLSConstants.Product.ZERO_DOLLAR.getCode(), TLSConstants.Product.HYBRID_MODEM.getCode(),
                TLSConstants.Product.HFC_NTD.getCode(), TLSConstants.Product.HARDWARE.getCode(), TLSConstants.Product.MHD_MLO.getCode(),
                TLSConstants.Product.MHD_MLO_DEVICE.getCode(), TLSConstants.Product.MHD_PRIORITY_ASSIST.getCode(),TLSConstants.Product.MHD_5G_FIXED_WLS.getCode() };
        List<String> productsList = Arrays.asList(productItemType);
        Set<OrderItemValue> assocOrderItems = new HashSet<>();
        for (OrderItemValue orderItem : orderValue.getOrderItem().getItem()) {
            OrderLineValue rootOrderLine = orderItem.getRootOrderLine();
            if (productsList.contains(TLSConstants.Product.from(rootOrderLine).getCode())) {
                assocOrderItems.add(orderItem);
            }
        }
        return assocOrderItems;
    }

    public static String getProductActionType(OrderItemValue orderItemValue){
        String productAction = null;
        if (null != orderItemValue.getRootOrderLine().getSubActionType() ) {
            String subActionType = orderItemValue.getRootOrderLine().getSubActionType().toUpperCase();
            switch (subActionType) {
                case "PR":
                    productAction = TLSConstants.ProductAction.ADD;
                    break;
                case "CH":
                    productAction = TLSConstants.ProductAction.MODIFY;
                    break;
                case "CE":
                    productAction = TLSConstants.ProductAction.CEASE;
                    break;
                default:
                    productAction = "";
            }
        }
        return productAction;
    }


}
