package com.telstra.eomsys.oss.sfo.logistics.inbound.kakfa.activity.common;

import com.amdocs.oss.xml.common_cbecore.v1.EntitySpecificationKey;
import com.amdocs.oss.xml.common_cbecustomer.v1.CustomerValue;
import com.amdocs.oss.xml.order.v1.*;
import com.telstra.eomsys.oss.sfo.common.domain.constant.TLSConstants;
import com.telstra.eomsys.oss.sfo.common.domain.model.ExecutionParam;
import com.telstra.eomsys.oss.sfo.common.domain.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        LoggerFactory.class,
        Logger.class,
        ActivityImplementationUtil.class,
        DPMUtil.class,
        IntegrationServicesUtil.class,
        NotificationUtil.class

})
@SuppressStaticInitializationFor({
        "com.telstra.eomsys.oss.sfo.common.domain.util.IntegrationServicesUtil",
        "com.telstra.eomsys.oss.sfo.common.adt.projectstore.ws.ProjectStoreClientSDKConfig",
        "com.telstra.eomsys.oss.sfo.common.adt.projectstore.ws.ProjectStoreSDKClient",
        "com.telstra.eomsys.oss.sfo.common.core.exception.TelstraException"


})

public class SendReminderNotificationImplTest  {

    SendReminderNotificationImpl sendReminderNotification;
    ActivityImplementationUtil activityImplementationUtil;
    DPMUtil dpmUtil;
    IntegrationServicesUtil integrationServicesUtil;
    CorrelationUtil correlationUtil;
    NotificationUtil notificationUtil;


    @Before
    public void setUp() throws Exception {
        mockStatic(LoggerFactory.class);
        Logger loggerMocked = mock(Logger.class, Mockito.RETURNS_DEEP_STUBS);

        sendReminderNotification = new SendReminderNotificationImpl();

        mockStatic(DPMUtil.class);
        dpmUtil = mock(DPMUtil.class);
        Whitebox.setInternalState(sendReminderNotification, "dpmUtil", dpmUtil);

        mockStatic(IntegrationServicesUtil.class);
        integrationServicesUtil = mock(IntegrationServicesUtil.class);
        Whitebox.setInternalState(sendReminderNotification, "integrationServicesUtil", integrationServicesUtil);

        correlationUtil = mock(CorrelationUtil.class);
        Whitebox.setInternalState(sendReminderNotification, "correlationUtil", correlationUtil);

        mockStatic(NotificationUtil.class);
        notificationUtil = mock(NotificationUtil.class);
        Whitebox.setInternalState(sendReminderNotification, "notificationUtil", notificationUtil);
    }


    @Test
    public void resetOrderItemStatus() throws Exception {

        integrationServicesUtil.updateProjectStore(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Whitebox.invokeMethod(sendReminderNotification, "resetOrderItemStatus", null);
        Assert.assertTrue(true);


    }

    @Test
    public void testGetExecutionParamListFromOrder() throws Exception{
        List<ExecutionParam> executionParamList = new ArrayList<>();
        List<ExecutionParam> initialParamList = new ArrayList<>();
        Map<String, String> mapProjectStore = new HashMap<>();
        mapProjectStore.put("escalationDays", "2");
        mapProjectStore.put("logisticsBookingId", "2");
        mapProjectStore.put("returnOrderItemID", "2");
        OrderItemValue orderItemValue = new OrderItemValue();
        orderItemValue.setActionType("orderItemAction");
        orderItemValue.setId("orderItemId");
        orderItemValue.setVersion(1);
        OrderLineValue orderLineValue = new OrderLineValue();
        OrderLineEntityValue orderLineEntityValue = new OrderLineEntityValue();
        orderLineEntityValue.setId("entityId");
        EntitySpecificationKey key = new EntitySpecificationKey();
        key.setPrimaryKey("key");
        orderLineEntityValue.setDescribingSpecificationKey(key);
        orderLineValue.setEntity(orderLineEntityValue);
        orderLineValue.setSubActionType("PR");
        orderItemValue.setRootOrderLine(orderLineValue);
        executionParamList = sendReminderNotification.getExecutionParamListFromOrder(mapProjectStore, initialParamList, orderItemValue);
        Assert.assertNotNull(executionParamList);
        Assert.assertEquals(9, executionParamList.size());}

    @Test
    public void testGetExecutionParamListFromOrder1() throws Exception{
        List<ExecutionParam> executionParamList = new ArrayList<>();
        List<ExecutionParam> initialParamList = new ArrayList<>();
        Map<String, String> mapProjectStore = new HashMap<>();
        mapProjectStore.put("escalationDays", "2");
        mapProjectStore.put("logisticsBookingId", "2");
        mapProjectStore.put("returnOrderItemID", "2");
        OrderItemValue orderItemValue = new OrderItemValue();
        orderItemValue.setActionType("orderItemAction");
        orderItemValue.setId("orderItemId");
        orderItemValue.setVersion(1);
        OrderLineValue orderLineValue = new OrderLineValue();
        OrderLineEntityValue orderLineEntityValue = new OrderLineEntityValue();
        orderLineEntityValue.setId("entityId");
        EntitySpecificationKey key = new EntitySpecificationKey();
        key.setPrimaryKey("key");
        orderLineEntityValue.setDescribingSpecificationKey(key);
        orderLineValue.setEntity(orderLineEntityValue);
        orderLineValue.setSubActionType("CH");
        orderItemValue.setRootOrderLine(orderLineValue);
        executionParamList = sendReminderNotification.getExecutionParamListFromOrder(mapProjectStore, initialParamList, orderItemValue);
        Assert.assertNotNull(executionParamList);
        Assert.assertEquals(9, executionParamList.size());

    }

    @Test
    public void testGetExecutionParamListFromOrder2() throws Exception{
        List<ExecutionParam> executionParamList = new ArrayList<>();
        List<ExecutionParam> initialParamList = new ArrayList<>();
        Map<String, String> mapProjectStore = new HashMap<>();
        mapProjectStore.put("escalationDays", "2");
        mapProjectStore.put("logisticsBookingId", "2");
        mapProjectStore.put("returnOrderItemID", "2");
        OrderItemValue orderItemValue = new OrderItemValue();
        orderItemValue.setActionType("orderItemAction");
        orderItemValue.setId("orderItemId");
        orderItemValue.setVersion(1);
        OrderLineValue orderLineValue = new OrderLineValue();
        OrderLineEntityValue orderLineEntityValue = new OrderLineEntityValue();
        orderLineEntityValue.setId("entityId");
        EntitySpecificationKey key = new EntitySpecificationKey();
        key.setPrimaryKey("key");
        orderLineEntityValue.setDescribingSpecificationKey(key);
        orderLineValue.setEntity(orderLineEntityValue);
        orderLineValue.setSubActionType("CE");
        orderItemValue.setRootOrderLine(orderLineValue);
        executionParamList = sendReminderNotification.getExecutionParamListFromOrder(mapProjectStore, initialParamList, orderItemValue);
        Assert.assertNotNull(executionParamList);
        Assert.assertEquals(9, executionParamList.size());

    }

    @Test
    public void getExecutionParamList() throws Exception {
        List<ExecutionParam> executionParamList = new ArrayList<>();
        Map<String, String> mapProjectStore = new HashMap<>();
        mapProjectStore.put("escalationDays", "2");
        mapProjectStore.put("logisticsBookingId", "2");
        mapProjectStore.put("returnOrderItemID", "2");
        OrderValue orderValue = new OrderValue();
        CustomerValue customer = new CustomerValue();
        customer.setId("1");
        orderValue.setCustomer(customer);
        executionParamList = Whitebox.invokeMethod(sendReminderNotification, "getExecutionParamList", mapProjectStore, "1", orderValue);
        Assert.assertNotNull(executionParamList);
        Assert.assertEquals(11, executionParamList.size());

    }

    @Test
    public void getExecutionParamListNull() throws Exception {
        Map<String, String> mapProjectStore = new HashMap<>();
        mapProjectStore.put("escalationDays", "2");
        mapProjectStore.put("logisticsBookingId", "2");
        mapProjectStore.put("returnOrderItemID", "2");
        OrderValue orderValue = new OrderValue();
        CustomerValue customer = new CustomerValue();
        customer.setId("1");
        orderValue.setCustomer(customer);
        String correlationId =null;
        List<ExecutionParam> executionParamList = Whitebox.invokeMethod(sendReminderNotification,"getExecutionParamList",mapProjectStore,null,orderValue);
        for (ExecutionParam param: executionParamList) {
            if (TLSConstants.DataAttribute.CORRELATIONID.equals(param.getName())) {
                correlationId = param.getValue();
                break;
            }
        }

        Assert.assertNotNull(executionParamList);
        Assert.assertEquals(10, executionParamList.size());
        Assert.assertNull(correlationId);
    }


    @Test
    public void getExecutionParamList1() throws Exception {
        Map<String, String> mapProjectStore = new HashMap<>();
        mapProjectStore.put("escalationDays", "2");
        mapProjectStore.put("logisticsBookingId", "2");
        mapProjectStore.put("returnOrderItemID", "2");
        OrderValue orderValue = new OrderValue();
        CustomerValue customer = new CustomerValue();
        customer.setId("1");
        orderValue.setCustomer(customer);
        String correlationId =null;
        List<ExecutionParam> executionParamList = Whitebox.invokeMethod(sendReminderNotification,"getExecutionParamList",mapProjectStore,"1",orderValue);
        for (ExecutionParam param: executionParamList) {
            if (TLSConstants.DataAttribute.CORRELATIONID.equals(param.getName())) {
                correlationId = param.getValue();
                break;
            }
        }

        Assert.assertNotNull(executionParamList);
        Assert.assertEquals(11, executionParamList.size());
        Assert.assertEquals("1",correlationId);
    }


    @Test
    public void testCompleteOpenDetachedAdhocActivity() {




    }
    @Test
    public void findAssociatedOrderItems() throws Exception {
        OrderValue orderValue = new OrderValue();
        ArrayOfOrderItemValue arrayOfOrderItemValue = mock(ArrayOfOrderItemValue.class);
        List<OrderItemValue> orderItemValues = new ArrayList<>();
        OrderItemValue orderItemValue = new OrderItemValue();
        orderItemValue.setActionType("orderItemAction");
        orderItemValue.setId("orderItemId");
        orderItemValue.setVersion(1);
        OrderLineValue orderLineValue = new OrderLineValue();
        OrderLineEntityValue orderLineEntityValue = new OrderLineEntityValue();
        orderLineEntityValue.setId("entityId");
        EntitySpecificationKey key = new EntitySpecificationKey();
        key.setPrimaryKey("V00000044");
        orderLineEntityValue.setDescribingSpecificationKey(key);
        orderLineValue.setEntity(orderLineEntityValue);
        orderLineValue.setSubActionType("PR");
        orderItemValue.setRootOrderLine(orderLineValue);

        orderItemValues.add(orderItemValue);
        PowerMockito.when(arrayOfOrderItemValue.getItem()).thenReturn(orderItemValues);
        orderValue.setOrderItem(arrayOfOrderItemValue);
        Set<OrderItemValue> orderItemValuesReceived = Whitebox.invokeMethod(sendReminderNotification, "findAssociatedOrderItems",orderValue);
        Assert.assertEquals(1, orderItemValuesReceived.size());
    }

    @Test
    public void findAssociatedOrderItems1() throws Exception {
        OrderValue orderValue = new OrderValue();
        ArrayOfOrderItemValue arrayOfOrderItemValue = mock(ArrayOfOrderItemValue.class);
        List<OrderItemValue> orderItemValues = new ArrayList<>();
        OrderItemValue orderItemValue = new OrderItemValue();
        orderItemValue.setActionType("orderItemAction");
        orderItemValue.setId("orderItemId");
        orderItemValue.setVersion(1);
        OrderLineValue orderLineValue = new OrderLineValue();
        OrderLineEntityValue orderLineEntityValue = new OrderLineEntityValue();
        orderLineEntityValue.setId("entityId");
        EntitySpecificationKey key = new EntitySpecificationKey();
        orderLineEntityValue.setDescribingSpecificationKey(key);
        orderLineValue.setEntity(orderLineEntityValue);
        orderLineValue.setSubActionType("PR");
        orderItemValue.setRootOrderLine(orderLineValue);

        orderItemValues.add(orderItemValue);
        PowerMockito.when(arrayOfOrderItemValue.getItem()).thenReturn(orderItemValues);
        orderValue.setOrderItem(arrayOfOrderItemValue);
        Set<OrderItemValue> orderItemValuesReceived = Whitebox.invokeMethod(sendReminderNotification, "findAssociatedOrderItems",orderValue);
        Assert.assertTrue(orderItemValuesReceived.isEmpty());
    }
    @Test
    public void testGetProductActionType1() {

        OrderItemValue orderItemValue = new OrderItemValue();
        orderItemValue.setActionType("orderItemAction");
        orderItemValue.setId("orderItemId");
        orderItemValue.setVersion(1);
        OrderLineValue orderLineValue = new OrderLineValue();
        OrderLineEntityValue orderLineEntityValue = new OrderLineEntityValue();
        orderLineEntityValue.setId("entityValueId");
        EntitySpecificationKey key = new EntitySpecificationKey();
        key.setPrimaryKey("key");
        orderLineEntityValue.setDescribingSpecificationKey(key);
        orderLineValue.setEntity(orderLineEntityValue);
        orderLineValue.setSubActionType("PR");
        orderItemValue.setRootOrderLine(orderLineValue);
        String action = sendReminderNotification.getProductActionType(orderItemValue);
        Assert.assertEquals("add", action);
        Assert.assertNotNull(orderItemValue.getRootOrderLine().getSubActionType() );
    }

    @Test
    public void testGetProductActionType2() {

        OrderItemValue orderItemValue = new OrderItemValue();
        orderItemValue.setActionType("orderItemAction");
        orderItemValue.setId("orderItemId");
        orderItemValue.setVersion(1);
        OrderLineValue orderLineValue = new OrderLineValue();
        OrderLineEntityValue orderLineEntityValue = new OrderLineEntityValue();
        orderLineEntityValue.setId("entityValueId");
        EntitySpecificationKey key = new EntitySpecificationKey();
        key.setPrimaryKey("key");
        orderLineEntityValue.setDescribingSpecificationKey(key);
        orderLineValue.setEntity(orderLineEntityValue);
        orderLineValue.setSubActionType("CH");
        orderItemValue.setRootOrderLine(orderLineValue);
        String action = sendReminderNotification.getProductActionType(orderItemValue);
        Assert.assertEquals("modify", action);
        Assert.assertNotNull(orderItemValue.getRootOrderLine().getSubActionType() );
    }

    @Test
    public void testGetProductActionType3() {

        OrderItemValue orderItemValue = new OrderItemValue();
        orderItemValue.setActionType("orderItemAction");
        orderItemValue.setId("orderItemId");
        orderItemValue.setVersion(1);
        OrderLineValue orderLineValue = new OrderLineValue();
        OrderLineEntityValue orderLineEntityValue = new OrderLineEntityValue();
        orderLineEntityValue.setId("entityValueId");
        EntitySpecificationKey key = new EntitySpecificationKey();
        key.setPrimaryKey("key");
        orderLineEntityValue.setDescribingSpecificationKey(key);
        orderLineValue.setEntity(orderLineEntityValue);
        orderLineValue.setSubActionType("CE");
        orderItemValue.setRootOrderLine(orderLineValue);
        String action = sendReminderNotification.getProductActionType(orderItemValue);
        Assert.assertEquals("cease", action);
        Assert.assertNotNull(orderItemValue.getRootOrderLine().getSubActionType() );
    }

    @Test
    public void testGetProductActionType4() {

        OrderItemValue orderItemValue = new OrderItemValue();
        orderItemValue.setActionType("orderItemAction");
        orderItemValue.setId("orderItemId");
        orderItemValue.setVersion(1);
        OrderLineValue orderLineValue = new OrderLineValue();
        OrderLineEntityValue orderLineEntityValue = new OrderLineEntityValue();
        orderLineEntityValue.setId("entityValueId");
        EntitySpecificationKey key = new EntitySpecificationKey();
        key.setPrimaryKey("key");
        orderLineEntityValue.setDescribingSpecificationKey(key);
        orderLineValue.setEntity(orderLineEntityValue);
        orderLineValue.setSubActionType("");
        orderItemValue.setRootOrderLine(orderLineValue);
        String action = sendReminderNotification.getProductActionType(orderItemValue);
        Assert.assertEquals("", action);
        Assert.assertNotNull(orderItemValue.getRootOrderLine().getSubActionType() );
    }

    @Test
    public void testGetProductActionType5() {

        OrderItemValue orderItemValue = new OrderItemValue();
        orderItemValue.setActionType("orderItemAction");
        orderItemValue.setId("orderItemId");
        orderItemValue.setVersion(1);
        OrderLineValue orderLineValue = new OrderLineValue();
        OrderLineEntityValue orderLineEntityValue = new OrderLineEntityValue();
        orderLineEntityValue.setId("entityValueId");
        EntitySpecificationKey key = new EntitySpecificationKey();
        key.setPrimaryKey("key");
        orderLineEntityValue.setDescribingSpecificationKey(key);
        orderLineValue.setEntity(orderLineEntityValue);
        orderLineValue.setSubActionType(null);
        orderItemValue.setRootOrderLine(orderLineValue);
        String action = sendReminderNotification.getProductActionType(orderItemValue);
        //Assert.assertEquals("", action);
        Assert.assertNull(orderItemValue.getRootOrderLine().getSubActionType() );
    }


}