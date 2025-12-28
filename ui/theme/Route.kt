package com.example.menuannam.ui.theme
import kotlinx.serialization.Serializable

// Define a home route that doesn't take any arguments
@Serializable
object HomeRoute

@Serializable
object AddCardRoute

@Serializable
object StudyCardsRoute

@Serializable
object SearchCardsRoute

// Define a showcardroute route that takes a english and a vietnamese word
@Serializable
data class ShowCardRoute(val english: String, val vietnamese: String)

@Serializable
data class EditCardRoute(val english: String, val vietnamese: String)

@Serializable
object LoginRoute