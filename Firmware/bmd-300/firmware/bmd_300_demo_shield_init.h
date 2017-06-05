/** @file bmd_300_eval_shield.h
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

#ifndef __BMD_300_EVAL_SHIELD_INIT_H__
#define __BMD_300_EVAL_SHIELD_INIT_H__

#include "ble_bmdeval.h"

#ifdef __cplusplus
extern "C" {
#endif
    
/** @brief Evaluation Shield initialization
 *
 * @param[in] p_bmdeval : Instance of the ble_bmdeval_t service
 *
 * @returns NRF_SUCCESS when successful
 *          Other errors possible
 */
uint32_t bmd300_demo_shield_init(ble_bmdeval_t * const p_bmdeval);
    
#ifdef __cplusplus
}
#endif

#endif
