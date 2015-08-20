// This file is for extra doxygen documentation that doesn't really
// fit anywhere else.

/** @file
 * 
 * @defgroup button_state Button State Monitor
 * @{
 * @ingroup button_state
 * @brief All button state monitoring items
 * @details
 */

 /**
 * @brief Button event handler module.
 * @details Value for button characteristic is for two buttons.
 * First button uses lowest bit.
 * Second button uses bit 5.
 *
 * @param[in] pin_no : Pin number
 *
 * @param[out] void
 */
void button_event_handler(uint8_t pin_no);

/**@brief Function for initializing the button handler module.
 *
 * @param[in] void
 *
 * @param[out] void
 */
void buttons_init(void);
/** @} */