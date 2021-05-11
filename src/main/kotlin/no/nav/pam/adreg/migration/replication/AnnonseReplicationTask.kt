package no.nav.pam.adreg.migration.replication

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.pam.feed.taskscheduler.FeedTaskService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.Month

@Component
@ConditionalOnProperty(name = ["migration.scheduler.enabled"], matchIfMissing = true)
class AnnonseReplicationTask(
    val feedtaskService: FeedTaskService,
    val annonseReplicationService: AnnonseReplicationService
    )
{

    companion object {
        const val TASKID = "AnnonseReplicationTask"
    }

    private val log = LoggerFactory.getLogger(AnnonseReplicationTask::class.java)

    @Scheduled(cron = "0 * * * * *")
    @SchedulerLock(name = TASKID, lockAtLeastFor = "PT15S", lockAtMostFor = "PT5M")
    fun executeUpdateBatch() {

        val updatedSince: LocalDateTime = feedtaskService.fetchLastRunDateForJob(TASKID).orElse(
            LocalDateTime.of(2013, Month.JUNE, 11, 8, 0))

        log.info("Execute update batch, updateSince = ${updatedSince}")

        val nextUpdatedTimestamp: LocalDateTime = annonseReplicationService.processUpdateBatchSince(updatedSince)?:updatedSince

        feedtaskService.save(TASKID, nextUpdatedTimestamp)

        log.info("Update batch finished, latest updated = ${nextUpdatedTimestamp}")
    }

    @Scheduled(cron = "30 */30 * * * *")
    @SchedulerLock(name = TASKID, lockAtLeastFor = "PT15S", lockAtMostFor = "PT30M")
    fun executeDeleteBatch() {
        log.info("Execute delete batch")

        val n = annonseReplicationService.processAllDeletes()

        log.info("Deleted ${n} annonser from local repository")
    }

}
