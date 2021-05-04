package no.nav.pam.adreg.migration.replication

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.pam.feed.taskscheduler.FeedTaskService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.Month

@Component
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

        val updatedSince = feedtaskService.fetchLastRunDateForJob(TASKID).orElse(
            LocalDateTime.of(2013, Month.JUNE, 11, 8, 0))

        log.info("Execute update batch, updateSince = ${updatedSince}")

        val latestTimestampInBatch = annonseReplicationService.processUpdateBatchSince(updatedSince)

        feedtaskService.save(TASKID, latestTimestampInBatch)

        log.info("Update batch finished, latest updated = ${latestTimestampInBatch}")
    }

    @Scheduled(cron = "30 */10 * * * *")
    @SchedulerLock(name = TASKID, lockAtLeastFor = "PT15S", lockAtMostFor = "PT30M")
    fun executeDeleteBatch() {
        log.info("Execute delete batch")

        val n = annonseReplicationService.processDeletes()

        log.info("Deleted ${n} annonser from local repository")
    }

}
