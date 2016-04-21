/** @file bmd_button.h
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

#ifndef __BMD_BUTTON_H__
#define __BMD_BUTTON_H__

#include <stdint.h>
#include "bsp.h"
#include "ble_bmdeval.h"

#ifdef __cplusplus
extern "C" {
#endif

/** @brief Initialize eval demo button handling
 *
 *  @param[in] p_bmdeval : Instance of ble_bmdeval_t service
 *
 *  @returns NRF_SUCCESS when successful
 *           Other error codes possible if button configuration is invalid
 */
uint32_t bmd_button_init(ble_bmdeval_t const * p_bmdeval);

/** @brief Reset internal button state
 *
 * @returns NRF_SUCCESS if successful
 *          NRF_ERROR_INVALID_STATE if not initialized
 */ 
uint32_t bmd_button_reset(void);

/** @brief Button event handler
 *
 * @returns NRF_SUCCESS if successful
 *          NRF_ERROR_INVALID_STATE if not initialized
 */
uint32_t bmd_button_event_handler(bsp_event_t event);

#ifdef __cplusplus
}
#endif

#endif
