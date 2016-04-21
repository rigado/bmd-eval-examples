#include <string.h>
#include <stdbool.h>

#include "unity.h"
#include "unity_fixture.h"
#include "bmd_accel.h"
#include "ble_bmdeval.h"
#include "nrf_delay.h"
#include "app_trace.h"

#define ONE_G_DELTA     5
#define ONE_G_8_BIT_8G_RANGE_VAL    (0xFF >> 4)   //At +/- 2g scale, 127 = 2g
                                                  //which implies that 63 is
                                                  //1g

static bool volatile m_callback_received = false;

TEST_GROUP(BmdAccel);

TEST_SETUP(BmdAccel)
{
    ble_bmdeval_t bmdeval;
    
    m_callback_received = false;
    (void)bmd_accel_init(&bmdeval);
    (void)bmd_accel_enable(true);
}

TEST_TEAR_DOWN(BmdAccel)
{
    m_callback_received = false;
    (void)bmd_accel_deinit();
}

static bool do_init(void)
{
    ble_bmdeval_t bmdeval;
    return bmd_accel_init(&bmdeval);
}

TEST(BmdAccel, InitializedStateAfterInit)
{
    ble_bmdeval_t bmdeval;
    bool initialized = bmd_accel_init(&bmdeval);
    TEST_ASSERT_TRUE(initialized);
}

TEST(BmdAccel, InitializedFalseOnNullInput)
{
    bool initialized = bmd_accel_init(NULL);
    TEST_ASSERT_FALSE(initialized);
}

TEST(BmdAccel, AccelDisabledAfterInit)
{
    do_init();
    TEST_ASSERT_FALSE(bmd_accel_is_enabled());
}

TEST(BmdAccel, InterruptDisabledAfterInit)
{
    do_init();
    TEST_ASSERT_FALSE(bmd_accel_is_enabled());
}

TEST(BmdAccel, DeInitTrueIfInitialized)
{
    TEST_ASSERT_TRUE(bmd_accel_deinit());
}

TEST(BmdAccel, DeInitFalseIfNotInitialized)
{
    (void)bmd_accel_deinit();
    TEST_ASSERT_FALSE(bmd_accel_deinit());
}

TEST(BmdAccel, AccelDisabledAfterDeInit)
{
    (void)bmd_accel_enable(true);
    (void)bmd_accel_deinit();
    TEST_ASSERT_FALSE(bmd_accel_is_enabled());
}

TEST(BmdAccel, InterruptDisabledAfterDeInit)
{
    (void)bmd_accel_enable_interrupt(true, NULL);
    (void)bmd_accel_deinit();
    TEST_ASSERT_FALSE(bmd_accel_is_int_enabled());
}

TEST(BmdAccel, EnabledAfterEnableSetTrue)
{
    uint32_t err = bmd_accel_enable(true);
    TEST_ASSERT_EQUAL(NRF_SUCCESS, err);
    
    TEST_ASSERT_TRUE(bmd_accel_is_enabled());
}

TEST(BmdAccel, DisabledAfterEnableSetFalse)
{
    uint32_t err = bmd_accel_enable(false);
    TEST_ASSERT_EQUAL(NRF_SUCCESS, err);
    
    TEST_ASSERT_FALSE(bmd_accel_is_enabled());
}

TEST(BmdAccel, DataReadyAfterEnableAndOneSampleWaitTime)
{
    nrf_delay_ms(90);
    TEST_ASSERT_TRUE(bmd_accel_is_data_ready());
}

TEST(BmdAccel, DataNotReadyImmediatelyAfterEnable)
{
    TEST_ASSERT_FALSE(bmd_accel_is_data_ready());
}

TEST(BmdAccel, GetDataReturnsOneG)
{
    accel_data_t accel_data;
    memset(&accel_data, 0, sizeof(accel_data));
    
    while(!bmd_accel_is_data_ready()) {}
    bmd_accel_data_get(&accel_data);
        
    TEST_ASSERT_INT8_WITHIN(ONE_G_DELTA, ONE_G_8_BIT_8G_RANGE_VAL, 
        (int8_t)accel_data.z_value);
}

TEST(BmdAccel, EnableInterruptReturnsInvalidWhenAccelNotEnabled)
{
    bmd_accel_enable(false);
    TEST_ASSERT_EQUAL(NRF_ERROR_INVALID_STATE, 
        bmd_accel_enable_interrupt(true, NULL));
}

TEST(BmdAccel, EnableInterruptReturnsInvalidStateWhenNotInitialized)
{
    (void)bmd_accel_deinit();
    TEST_ASSERT_EQUAL(NRF_ERROR_INVALID_STATE, 
        bmd_accel_enable_interrupt(true, NULL));
}

TEST(BmdAccel, EnableInterruptReturnsSuccessWhenAccelEnabled)
{
    TEST_ASSERT_EQUAL(NRF_SUCCESS, bmd_accel_enable_interrupt(true, NULL));
}


static void data_ready_cb(void)
{
    m_callback_received = true;
}

static void wait_for_int_callback(void)
{
    uint8_t wait_count = 100;
    while(wait_count > 1 && !m_callback_received)
    {
        nrf_delay_ms(1);
        wait_count--;
    }
}

TEST(BmdAccel, InterruptCallbackCalledWhenInterruptEnabled)
{
    bmd_accel_enable_interrupt(true, data_ready_cb);
    
    wait_for_int_callback();
    
    TEST_ASSERT_TRUE(m_callback_received);
}

TEST(BmdAccel, InterruptNotCalledWhenCallbackNotPresent)
{
    bmd_accel_enable_interrupt(true, NULL);
    
    wait_for_int_callback();
    
    TEST_ASSERT_FALSE(m_callback_received);
}

TEST(BmdAccel, InterruptIsEnabledAfterEnable)
{
    bmd_accel_enable_interrupt(true, NULL);
    
    TEST_ASSERT_TRUE(bmd_accel_is_int_enabled());
}

TEST(BmdAccel, InterruptIsDisabledAfterDisable)
{
    bmd_accel_enable_interrupt(false, NULL);
    TEST_ASSERT_FALSE(bmd_accel_is_int_enabled());
}

TEST_GROUP_RUNNER(BmdAccel)
{
    RUN_TEST_CASE(BmdAccel, InitializedStateAfterInit);
    RUN_TEST_CASE(BmdAccel, InitializedFalseOnNullInput);
    RUN_TEST_CASE(BmdAccel, AccelDisabledAfterInit);
    RUN_TEST_CASE(BmdAccel, InterruptDisabledAfterInit);
    RUN_TEST_CASE(BmdAccel, DeInitTrueIfInitialized);
    RUN_TEST_CASE(BmdAccel, DeInitFalseIfNotInitialized);
    RUN_TEST_CASE(BmdAccel, AccelDisabledAfterDeInit);
    RUN_TEST_CASE(BmdAccel, InterruptDisabledAfterDeInit);
    RUN_TEST_CASE(BmdAccel, EnabledAfterEnableSetTrue);
    RUN_TEST_CASE(BmdAccel, DisabledAfterEnableSetFalse);
    RUN_TEST_CASE(BmdAccel, DataReadyAfterEnableAndOneSampleWaitTime);
    RUN_TEST_CASE(BmdAccel, DataNotReadyImmediatelyAfterEnable);
    RUN_TEST_CASE(BmdAccel, GetDataReturnsOneG);
    RUN_TEST_CASE(BmdAccel, EnableInterruptReturnsInvalidWhenAccelNotEnabled);
    RUN_TEST_CASE(BmdAccel, 
        EnableInterruptReturnsInvalidStateWhenNotInitialized);
    RUN_TEST_CASE(BmdAccel, EnableInterruptReturnsSuccessWhenAccelEnabled);
    RUN_TEST_CASE(BmdAccel, InterruptCallbackCalledWhenInterruptEnabled);
    RUN_TEST_CASE(BmdAccel, InterruptNotCalledWhenCallbackNotPresent);
    RUN_TEST_CASE(BmdAccel, InterruptIsEnabledAfterEnable);
    RUN_TEST_CASE(BmdAccel, InterruptIsDisabledAfterDisable);
}
