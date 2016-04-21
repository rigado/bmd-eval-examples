/** @file bmd_300_demo_shield.c
*
* @brief This module initializes all eval shield specific modules.
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

#include "sdk_common.h"

#include "ble_bmdeval.h"
#include "bmd_accel.h"
#include "bmd_adc.h"
#include "bmd_pwm_rgb_led.h"
#include "bmd_button.h"

#include "bmd_300_demo_shield_init.h"

uint32_t bmd300_demo_shield_init(ble_bmdeval_t * const p_bmdeval)
{
    uint32_t err;
    
    bool initialized = bmd_accel_init(p_bmdeval);
    err = (initialized) ? NRF_SUCCESS : NRF_ERROR_INTERNAL;
    VERIFY_SUCCESS(err);
    
    err = bmd_adc_init(p_bmdeval);
    VERIFY_SUCCESS(err);
    
    err = bmd_pwm_rgb_led_init();
    VERIFY_SUCCESS(err);
    
    err = bmd_button_init(p_bmdeval);
    VERIFY_SUCCESS(err);
    
    return err;
}
