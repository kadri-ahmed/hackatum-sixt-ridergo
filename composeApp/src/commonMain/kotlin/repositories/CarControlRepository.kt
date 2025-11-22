package repositories

import utils.NetworkError
import utils.Result

/**
 * Repository interface for car control operations.
 * Abstracts data sources (API) from business logic.
 */
interface CarControlRepository {
    /**
     * Locks the car.
     * @return Result indicating success or an error
     */
    suspend fun lockCar(): Result<Unit, NetworkError>
    
    /**
     * Unlocks the car.
     * @return Result indicating success or an error
     */
    suspend fun unlockCar(): Result<Unit, NetworkError>
    
    /**
     * Makes the car blink (flash lights).
     * @return Result indicating success or an error
     */
    suspend fun blinkCar(): Result<Unit, NetworkError>
}
