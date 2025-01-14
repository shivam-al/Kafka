package org.kafka.domain.observers.summary

import com.kafka.data.dao.ItemDetailDao
import com.kafka.data.entities.Summary
import com.kafka.data.feature.ai.OpenAiRepository
import com.kafka.data.feature.ai.SummaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.kafka.base.CoroutineDispatchers
import org.kafka.base.domain.SubjectInteractor
import javax.inject.Inject

class ObserveSummary @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val itemDetailDao: ItemDetailDao,
    private val openAiRepository: OpenAiRepository,
    private val dispatchers: CoroutineDispatchers,
) : SubjectInteractor<String, Summary?>() {

    override fun createObservable(params: String): Flow<Summary?> {
        return summaryRepository.observeSummary(params)
            .flatMapLatest { summary ->
                if (summary?.content != null) {
                    flowOf(summary)
                } else {
                    getSummary(params)
                }
            }
            .flowOn(dispatchers.io)
    }

    private suspend fun getSummary(itemId: String): Flow<Summary?> {
        val item = itemDetailDao.get(itemId)

        return openAiRepository.observerSummary(item.title.orEmpty(), item.creator, item.language)
            .onEach { response ->
                if (response.finished) {
                    val summary = Summary(itemId = itemId, content = response.content)
                    summaryRepository.updateSummary(summary)
                }
            }
            .map { response -> Summary(itemId = itemId, content = response.content) }
    }
}
