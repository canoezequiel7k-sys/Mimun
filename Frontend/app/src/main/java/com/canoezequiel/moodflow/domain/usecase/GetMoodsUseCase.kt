package com.canoezequiel.moodflow.domain.usecase

import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.domain.repository.MoodRepository


//Caso de uso para obtener la lista de estados de animo disponibles en el dominio
class GetMoodsUseCase(
    private val repository: MoodRepository
){
    //Operador invoke permite invocar el caso de uso como si fuera una funcion
    operator fun invoke(): List<Mood> {
        return repository.getModel()
    }
}