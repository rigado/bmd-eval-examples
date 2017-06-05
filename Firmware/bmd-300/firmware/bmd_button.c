/** @file bmd_button.c
*
* @brief This module provides interface functions for managing button presses
*        on Button1 and Button2 on the BMD-300 Evaluation Board.
*
* @par
* COPYRIGHT NOTICE: (c) Rigado
* All rights reserved. 
*
* Source code licensed under Software License Agreement in license.txt.
* You should have received a copy with purchase of BMD series product 
* and with this repository.  If not, contact modules@rigado.com.
*/

#include <stdint.h>
#include <stdbool.h>

#include "bsp.h"
#include "ble_bmdeval.h"
#include "sdk_common.h"

#include "bmd_button.h"

/* Pins for buttons found in pca10040.h; these are the bsp button ids */
#define EVAL_BUTTON0                     0
#define EVAL_BUTTON1                     1

#define EVAL_BUTTON0_MASK                0x10
#define EVAL_BUTTON1_MASK                0x01

static uint8_t m_button_state;
static bool m_initialized;
static ble_bmdeval_t const * mp_bmdeval;

uint32_t bmd_button_init(ble_bmdeval_t const * p_bmdeval)
{
    if(NULL == p_bmdeval)
    {
        return NRF_ERROR_NULL;
    }
    
    uint32_t err;
    err = bsp_event_to_button_action_assign(EVAL_BUTTON0, BSP_BUTTON_ACTION_PUSH, 
        BSP_EVENT_KEY_0);
    VERIFY_SUCCESS(err);
    
    err = bsp_event_to_button_action_assign(EVAL_BUTTON0, BSP_BUTTON_ACTION_RELEASE,
        BSP_EVENT_KEY_1);
    VERIFY_SUCCESS(err);
    
    err = bsp_event_to_button_action_assign(EVAL_BUTTON1, BSP_BUTTON_ACTION_PUSH, 
        BSP_EVENT_KEY_2);
    VERIFY_SUCCESS(err);
    
    err = bsp_event_to_button_action_assign(EVAL_BUTTON1, BSP_BUTTON_ACTION_RELEASE,
        BSP_EVENT_KEY_3);
    VERIFY_SUCCESS(err);
    
    mp_bmdeval = p_bmdeval;
    m_initialized = true;
    
    return NRF_SUCCESS;
}

uint32_t bmd_button_reset()
{
    if(!m_initialized)
    {
        return NRF_ERROR_INVALID_STATE;
    }
    
    m_button_state = 0;
    
    return NRF_SUCCESS;
}

uint32_t bmd_button_event_handler(bsp_event_t event)
{
    if(!m_initialized)
    {
        return NRF_ERROR_INVALID_STATE;
    }
    
    switch(event)
    {
        case BSP_EVENT_KEY_0:
            m_button_state |= EVAL_BUTTON0_MASK;
            (void)ble_bmdeval_on_button_change(mp_bmdeval, m_button_state);
            break;
        case BSP_EVENT_KEY_1:
            m_button_state &= ~EVAL_BUTTON0_MASK;
            (void)ble_bmdeval_on_button_change(mp_bmdeval, m_button_state);
            break;
        case BSP_EVENT_KEY_2:
            m_button_state |= EVAL_BUTTON1_MASK;
            (void)ble_bmdeval_on_button_change(mp_bmdeval, m_button_state);
            break;
        case BSP_EVENT_KEY_3:
            m_button_state &= ~EVAL_BUTTON1_MASK;
            (void)ble_bmdeval_on_button_change(mp_bmdeval, m_button_state);
            break;
        default:
            break;
    }
    
    return NRF_SUCCESS;
}   
