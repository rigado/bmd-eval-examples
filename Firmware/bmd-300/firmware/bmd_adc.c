/** @file bmd_adc.c
*
* @brief This module implements the SAADC read 8-bit analog values from the 
*        BMD-300 EVAL demo shield ambient light sensor. This module is set up 
*        to use 8x oversampling.  Conversions are performed at the rate defined
*        by SAMPLE_RATE_MS.
*
* @par
* COPYRIGHT NOTICE: (c) Rigado
* All rights reserved. 
*
* Source code licensed under Software License Agreement in license.txt.
* You should have received a copy with purchase of BMD series product 
* and with this repository.  If not, contact modules@rigado.com.
*/

#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

#include "app_util_platform.h"
#include "app_error.h"
#include "app_timer.h"
#include "boards.h"
#include "nrf.h"
#include "nrf_adc.h"
#include "nrf_drv_saadc.h"
#include "nrf_error.h"

#include "bmd_300_demo_shield.h"

#include "bmd_adc.h"

/**
 * @defgroup adc_module ADC Module
 * @{
 * @ingroup adc_module
 * @brief All ADC reading specific items
 * @details
 */
static ble_bmdeval_t * mp_bmdeval;	
static bool m_initialized;
static bool	m_streaming;

static volatile nrf_saadc_value_t m_cur_sample;

APP_TIMER_DEF(m_adc_sample_timer);

static void adc_sample_timer_handler(void * p_context);
static void saadc_event_handler(nrf_drv_saadc_evt_t const * p_event);

uint32_t bmd_adc_init(ble_bmdeval_t * p_bmdeval)
{
    bmd_adc_deinit();
    
    nrf_saadc_channel_config_t channel_config =
    NRF_DRV_SAADC_DEFAULT_CHANNEL_CONFIG_SE(
        BMD_300_DEMO_SHIELD_AMBIENT_LIGHT_AIN);
    nrf_drv_saadc_config_t saadc_config = NRF_DRV_SAADC_DEFAULT_CONFIG;
    saadc_config.resolution = NRF_SAADC_RESOLUTION_8BIT;
    //saadc_config.oversample = NRF_SAADC_OVERSAMPLE_8X;
    uint32_t err_code = nrf_drv_saadc_init(&saadc_config, saadc_event_handler);
    if(NRF_SUCCESS != err_code)
    {
        return err_code;
    }
    
    channel_config.acq_time = NRF_SAADC_ACQTIME_40US;
    err_code = nrf_drv_saadc_channel_init(0, &channel_config);
    if(NRF_SUCCESS != err_code)
    {
        return err_code;
    }
    
    //Driver and saadc hal layers do not provide anyway to configure burst
    //mode
    NRF_SAADC->CH[0].CONFIG |= SAADC_CH_CONFIG_BURST_Msk;
    
    err_code = app_timer_create(&m_adc_sample_timer, APP_TIMER_MODE_SINGLE_SHOT, 
                                adc_sample_timer_handler);
    if(NRF_SUCCESS != err_code)
    {
        return err_code;
    }
    
    mp_bmdeval = p_bmdeval;
    m_initialized = true;
    
    return NRF_SUCCESS;
}
/**
 * @brief ADC deinitialization.
 */
uint32_t bmd_adc_deinit(void)
{
    if(!m_initialized)
    {
        return NRF_ERROR_INVALID_STATE;
    }
    
    nrf_drv_saadc_abort();
    nrf_drv_saadc_uninit();
    
    m_initialized = false;
    mp_bmdeval = NULL;
    
    return NRF_SUCCESS;
}

/**
 * @brief Function for starting the four ADC readings
 */
uint32_t bmd_adc_set_streaming_state(bool state)
{
    if(!m_initialized)
    {
        return NRF_ERROR_INVALID_STATE;
    }
    
    if(state && m_streaming)
    {
        return NRF_SUCCESS;
    }
    
    m_streaming = state;
    
    if(m_streaming)
    {
        app_timer_start(m_adc_sample_timer, APP_TIMER_TICKS(SAMPLE_RATE_MS, 0), 
                        NULL);
    }
    else
    {
        app_timer_stop(m_adc_sample_timer);
        nrf_drv_saadc_abort();
    }
    
    return NRF_SUCCESS;
}

static void adc_sample_timer_handler(void * p_context)
{
    if(!m_initialized)
    {
        return;
    }
    
    nrf_drv_saadc_buffer_convert((nrf_saadc_value_t *)&m_cur_sample, 1);
    nrf_drv_saadc_sample();
}

static void saadc_event_handler(nrf_drv_saadc_evt_t const * p_event)
{
    if(!m_initialized)
    {
        return;
    }
    
    if(p_event->type == NRF_DRV_SAADC_EVT_DONE)
    {
        ble_bmdeval_on_adc_change(mp_bmdeval, (uint8_t)m_cur_sample);
        app_timer_start(m_adc_sample_timer, APP_TIMER_TICKS(SAMPLE_RATE_MS, 0),
                        NULL);
    }
}

/** @} */
