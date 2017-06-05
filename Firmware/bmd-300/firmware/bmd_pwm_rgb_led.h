/** @file bmd_pwm_rgb_led.h
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

#ifndef __BMD_PWM_RGB_LED_H__
#define __BMD_PWM_RGB_LED_H__

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif
    
typedef struct rgb_color_s
{
    uint8_t red;
    uint8_t green;
    uint8_t blue;
} bmd_pwm_rgb_color_t;

/**
 * @{
 * @ingroup bmd_pwm_rgb_led
 *
 * @brief
 * Function: bmd_pwm_rgb_led_init
 *
 * Description: Initialize PWM driver to drive an RGB LED.
 *
 * @returns NRF_SUCCESS if successful
 *          Other errors possible if nrf_pwm_drv_init fails
 */
uint32_t bmd_pwm_rgb_led_init(void);

/**
 * @brief
 * Function: bmd_pwm_rgb_led_is_initialized
 *
 * Description: Retruns the initialized state
 *
 * @returns true if initialized; false otherwise
 */
bool bmd_pwm_rgb_led_is_initialized(void);

/**
 * @brief
 * Function: bmd_pwm_rgb_led_deinit
 *
 * Description: De-initialize pwm driver
 *
 * @returns NRF_SUCCESS if successful
 *          NRF_ERROR_INVALID_STATE if not initialized
 */
uint32_t bmd_pwm_rgb_led_deinit(void);

/**
 * @brief
 * Function: bmd_pwm_rgb_led_start
 *
 * Description: Used to set PWM values for and activate the RGB LED.
 *
 * @param[in] starting_color : The starting color of the RGB LED
 *
 * @returns NRF_SUCCESS if successful
 *          NRF_ERROR_INVALID_STATE if not initialized
 */
uint32_t bmd_pwm_rgb_led_start(const bmd_pwm_rgb_color_t * starting_color);

/**
 * @brief
 * Function: bmd_pwm_rgb_led_set_color
 *
 * Description: Sets the color of the RGB led
 *
 * @param[in] color : The color to set on the RGB led
 *
 * @returns NRF_SUCCESS if successful
 *          NRF_ERROR_INVALID_STATE if not initialized
 */
uint32_t bmd_pwm_rgb_led_set_color(const bmd_pwm_rgb_color_t * color);

/**
 * @brief
 * Function: bmd_pwm_rgb_led_stop
 *
 * Description: Stops pwm playback.  Waits for PWM hardware to report that it
 *              has stopped.
 *
 * @returns NRF_SUCCESS if successful
 *          NRF_ERROR_INVALID_STATE if not initialized
 *          NRF_ERROR_INTERNAL if pwm could not be stopped
 */
uint32_t bmd_pwm_rgb_led_stop(void);

#ifdef __cplusplus
}
#endif
/** @} */

#endif
