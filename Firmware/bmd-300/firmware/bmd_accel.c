/** @file bmd_accel.c
*
* @brief This module implements the accelerometer on the BMD-200 EVAL 
*        board and provides an example on implementing I2C on an BMD-300 module.
*
* @par
* COPYRIGHT NOTICE: (c) Rigado
* All rights reserved. 
*
* Source code licensed under Software License Agreement in license.txt.
* You should have received a copy with purchase of BMD series product 
* and with this repository.  If not, contact modules@rigado.com.
*/

#include <string.h>
#include <nrf.h>
#include <nrf_soc.h>

#include "app_error.h"
#include "app_util_platform.h"
#include "nrf_gpio.h"
#include "nrf_drv_gpiote.h"
#include "nrf_nvic.h"
#include "MMA8652.h"

#include "bmd_300_demo_shield.h"

#include "bmd_accel.h"

static bool initialized;
static bool m_interrupt_enabled;

static void (*m_accel_data_ready_cb)(void);

/* Helper functions */
static void fill_accel_data(accel_data_t * const p_accel_data, uint8_t x,
                            uint8_t y, uint8_t z);

static void accel_int_handler(nrf_drv_gpiote_pin_t pin, 
                              nrf_gpiote_polarity_t action);

bool bmd_accel_init(ble_bmdeval_t * p_bmdeval)
{    
    if(NULL == p_bmdeval)
    {
        return false;
    }
    
    initialized = false;
    m_interrupt_enabled = false;
    m_accel_data_ready_cb = NULL;
    
    nrf_drv_gpiote_in_config_t config = GPIOTE_CONFIG_IN_SENSE_HITOLO(false);
    config.is_watcher = true;
    config.pull = NRF_GPIO_PIN_PULLUP;
    nrf_drv_gpiote_in_init(BMD_300_DEMO_SHIELD_ACCEL_INT_1, 
        &config, accel_int_handler);
    
    //Initialize the Accelerometer
	initialized = MMA8652Init();
	
    return initialized;
}

bool bmd_accel_deinit()
{
    if(!initialized)
    {
        return false;
    }
	
    nrf_drv_gpiote_in_uninit(BMD_300_DEMO_SHIELD_ACCEL_INT_1);
    
    MMA8652Deinit();
    
    m_accel_data_ready_cb = NULL;
    m_interrupt_enabled = false;
	initialized = false;
    
    return true;
}

uint32_t bmd_accel_enable(bool state)
{
    if(!initialized)
    {
        return NRF_ERROR_INVALID_STATE;
    }
    
    return MMA8652EnableDataReadyMode(state);
}

bool bmd_accel_is_enabled()
{
    if(!initialized)
    {
        return false;
    }
    
    uint8_t ctrl_reg_1 = 0;
    MMA8652ReadReg(MMA8652_CTRL_REG1_ADDR, &ctrl_reg_1, sizeof(ctrl_reg_1));
    return (ctrl_reg_1 & 0x01);
}

bool bmd_accel_is_data_ready()
{
    uint32_t pin_state = nrf_gpio_pin_read(BMD_300_DEMO_SHIELD_ACCEL_INT_1);
    return (pin_state == 0);
}

uint32_t bmd_accel_enable_interrupt(bool state, void (*accel_data_ready_cb)(void))
{
    if(!initialized || !bmd_accel_is_enabled())
    {
        return NRF_ERROR_INVALID_STATE;
    }
    
    if(state)
    {
        m_interrupt_enabled = true;
        m_accel_data_ready_cb = accel_data_ready_cb;
        nrf_drv_gpiote_in_event_enable(BMD_300_DEMO_SHIELD_ACCEL_INT_1, false);
    }
    else
    {
        m_interrupt_enabled = false;
        m_accel_data_ready_cb = NULL;
        nrf_drv_gpiote_in_event_disable(BMD_300_DEMO_SHIELD_ACCEL_INT_1);
    }
    
    return NRF_SUCCESS;
}

bool bmd_accel_is_int_enabled()
{
    return m_interrupt_enabled;
}

uint32_t bmd_accel_data_get(accel_data_t * const p_accel_data)
{
    int8_t data[3];
    
    memset(data, 0, sizeof(data));
	MMA8652ReadAllAxisData(data);
    
    fill_accel_data(p_accel_data, data[0], data[1], data[2]);
	
	return NRF_SUCCESS;
}

static void fill_accel_data(accel_data_t * const p_accel_data, uint8_t x,
                            uint8_t y, uint8_t z)
{
    p_accel_data->x_value = x;
    p_accel_data->y_value = y;
    p_accel_data->z_value = z;
}

static void accel_int_handler(nrf_drv_gpiote_pin_t pin, 
                              nrf_gpiote_polarity_t action)
{
    if(pin == BMD_300_DEMO_SHIELD_ACCEL_INT_1 && m_accel_data_ready_cb != NULL)
    {
        m_accel_data_ready_cb();
    }
}
