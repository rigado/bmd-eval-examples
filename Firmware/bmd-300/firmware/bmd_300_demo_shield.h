/** @file bmd_300_demo_shield.h
*
* @brief Defines all pins used by the BMD-300 Demo Shield.
*
* @par
* COPYRIGHT NOTICE: (c) Rigado
* All rights reserved. 
*
* Source code licensed under Software License Agreement in license.txt.
* You should have received a copy with purchase of BMD series product 
* and with this repository.  If not, contact modules@rigado.com.
*/

#ifndef _BMD_300_DEMO_SHIELD_H_
#define _BMD_300_DEMO_SHIELD_H_

#include "nrf_saadc.h"

#ifdef __cplusplus
extern "C" {
#endif

#define BMD_300_DEMO_SHIELD_ACCEL_INT_1         2

#define BMD_300_DEMO_SHIELD_AMBIENT_LIGHT       4
#define BMD_300_DEMO_SHIELD_AMBIENT_LIGHT_AIN   NRF_SAADC_INPUT_AIN2
    
#define BMD_300_DEMO_SHIELD_N_EE_CS             22
#define BMD_300_DEMO_SHIELD_EE_MOSI             23  
#define BMD_300_DEMO_SHIELD_EE_MISO             24
#define BMD_300_DEMO_SHIELD_EE_CLK              25
    
#define BMD_300_DEMO_SHIELD_ACCEL_SDA           26
#define BMD_300_DEMO_SHIELD_ACCEL_SCL           27
    
#define BMD_300_DEMO_SHIELD_LED_RED             31
#define BMD_300_DEMO_SHIELD_LED_GREEN           30
#define BMD_300_DEMO_SHIELD_LED_BLUE            29
    
#ifdef __cpluscplus
}
#endif
#endif
