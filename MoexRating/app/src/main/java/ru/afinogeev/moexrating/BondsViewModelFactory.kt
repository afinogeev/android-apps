package ru.afinogeev.moexrating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.afinogeev.moexrating.repository.Repository

class BondsViewModelFactory(private val repository: Repository): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BondsViewModel(repository) as T
    }
}