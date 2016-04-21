/** @file bmd_adc.h
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
#include <stdint.h>

#include "ble_bmdeval.h"

/**
 * @defgroup bmd_accel Accelerometer Driver
 * @{
 * @ingroup bmd_adc_module
 */		
 
 #define SAMPLE_RATE_MS     (125)
 
/**
 * @details
 * Function: bmd_adc_init
 *
 * Description: Function for Initializing the ADC module
 *
 * Assumptions: None
 *
 * @param[in] p_bmdeval : Pointer to bluetooth service structure that will be used to transmit data.
 *
 * @returns NRF_SUCCESS if successful
 *          Other errors possible if driver setup fails                         
 */
uint32_t bmd_adc_init(ble_bmdeval_t * p_bmdeval);

/**
 * @details
 * Function: bmd_adc_deinit
 *
 * Description: Function for Deinitializing the ADC module
 *
 * @returns NRF_SUCCESS if successful
 *          NRF_ERROR_INVALID_STATE if not initialized
 */
uint32_t bmd_adc_deinit(void);

/**
 * @details
 * Function: bmd_adc_set_streaming_state
 *
 * Description: Function for starting or stopping adc streaming
 *
 * @param[in] state : starts streaming if true, stops if false
 *
 * @returns NRF_SUCCESS if successful
 *          NRF_ERROR_INVALID_STATE if not initialized
 */
uint32_t bmd_adc_set_streaming_state(bool state);
/** @} */
