/** @file bmd_pwm_rgb_led.c
*
* @brief This module implements a PWM driven RGB LED using the built-in PWM
*        peripheral on the BMD-300.
*
* @par
* COPYRIGHT NOTICE: (c) Rigado
* All rights reserved. 
*
* Source code licensed under Software License Agreement in license.txt.
* You should have received a copy with purchase of BMD series product 
* and with this repository.  If not, contact modules@rigado.com.
*/

#include <nrf.h>
#include <nrf_soc.h>

#include "app_timer.h"
#include "app_util_platform.h"
#include "boards.h"
#include "nrf_drv_pwm.h"

#include "bmd_300_demo_shield.h"

#include "bmd_pwm_rgb_led.h"

#define PWM_CONFIG_TOP              0xFF
#define PWM_MAX_VALUE           	0x0
#define PWM_MIN_VALUE           	0xFF

static bool m_initialized;

static nrf_drv_pwm_t m_pwm_inst = NRF_DRV_PWM_INSTANCE(0);
static nrf_pwm_values_individual_t m_seq_values =
{ 
    PWM_MIN_VALUE,      
    PWM_MIN_VALUE,      
    PWM_MIN_VALUE,      
    0 
};

uint32_t bmd_pwm_rgb_led_init(void)
{
    nrf_drv_pwm_config_t const config =
    {
        .output_pins =
        {
            BMD_300_DEMO_SHIELD_LED_RED, // channel 0
            BMD_300_DEMO_SHIELD_LED_GREEN, // channel 1
            BMD_300_DEMO_SHIELD_LED_BLUE, // channel 2
            0xFF,
        },
        .irq_priority = APP_IRQ_PRIORITY_LOW,
        .base_clock   = NRF_PWM_CLK_8MHz,
        .count_mode   = NRF_PWM_MODE_UP,
        .top_value    = PWM_CONFIG_TOP,
        .load_mode    = NRF_PWM_LOAD_INDIVIDUAL,
        .step_mode    = NRF_PWM_STEP_AUTO
    };
    uint32_t err = nrf_drv_pwm_init(&m_pwm_inst, &config, NULL);
    if(err != NRF_SUCCESS)
    {
        return err;
    }
    
	m_initialized = true;
    
    return NRF_SUCCESS;
}

uint32_t bmd_pwm_rgb_led_deinit()
{
    if(!m_initialized)
    {
        return NRF_ERROR_INVALID_STATE;
    }
    nrf_drv_pwm_uninit(&m_pwm_inst);
    m_initialized = false;
    
    return NRF_SUCCESS;
}

bool bmd_pwm_rgb_led_is_initialized(void)
{
    return m_initialized;
}

uint32_t bmd_pwm_rgb_led_start(const bmd_pwm_rgb_color_t * starting_color)
{
    if(!m_initialized)
    {
        return NRF_ERROR_INVALID_STATE;
    }
    
    nrf_pwm_sequence_t const seq =
    {
        .values.p_individual = &m_seq_values,
        .length              = sizeof(m_seq_values) / sizeof(m_seq_values.channel_0),
        .repeats             = 0,
        .end_delay           = 0
    };

    nrf_drv_pwm_simple_playback(&m_pwm_inst, &seq, 1, NRF_DRV_PWM_FLAG_LOOP);
    
    return NRF_SUCCESS;
}

uint32_t bmd_pwm_rgb_led_set_color(const bmd_pwm_rgb_color_t * rgb_data)
{
    if(!m_initialized)
    {
        return NRF_ERROR_INVALID_STATE;
    }
    
    m_seq_values.channel_0 = rgb_data->red;
    m_seq_values.channel_1 = rgb_data->green;
    m_seq_values.channel_2 = rgb_data->blue;
    
    return NRF_SUCCESS;
}

uint32_t bmd_pwm_rgb_led_stop(void)
{ 
    if(!m_initialized)
    {
        return NRF_ERROR_INVALID_STATE;
    }
    
    bool stopped = nrf_drv_pwm_stop(&m_pwm_inst, true);
    
    if(!stopped)
    {
        return NRF_ERROR_INTERNAL;
    }
    
    return NRF_SUCCESS;
}
