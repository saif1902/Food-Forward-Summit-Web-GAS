package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM attendee_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<ProfileEntity?>

    @Query("SELECT * FROM attendee_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)
}

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agenda_sessions ORDER BY startTime ASC")
    fun getAllSessionsFlow(): Flow<List<AgendaSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<AgendaSession>)

    @Query("UPDATE agenda_sessions SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkState(id: String, isBookmarked: Boolean)
}

@Dao
interface MeetingDao {
    @Query("SELECT * FROM booked_meetings ORDER BY id DESC")
    fun getAllMeetingsFlow(): Flow<List<BookedMeeting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: BookedMeeting)

    @Query("DELETE FROM booked_meetings WHERE id = :id")
    suspend fun deleteMeetingById(id: Int)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM scanned_contacts ORDER BY scannedAt DESC")
    fun getAllContactsFlow(): Flow<List<ScannedContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ScannedContact)

    @Query("UPDATE scanned_contacts SET notes = :notes, rank = :rank WHERE id = :id")
    suspend fun updateContact(id: String, notes: String, rank: String)

    @Query("DELETE FROM scanned_contacts WHERE id = :id")
    suspend fun deleteContact(id: String)
}

@Dao
interface SocialPostDao {
    @Query("SELECT * FROM social_posts ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<SocialPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: SocialPost)

    @Query("DELETE FROM social_posts WHERE id = :id")
    suspend fun deletePostById(id: Int)

    @Query("UPDATE social_posts SET likesCount = :likesCount, isLikedByMe = :isLikedByMe WHERE id = :id")
    suspend fun updateLikesState(id: Int, likesCount: Int, isLikedByMe: Boolean)
}

@Dao
interface GamificationDao {
    @Query("SELECT * FROM gamification_state WHERE id = 1 LIMIT 1")
    fun getGameStateFlow(): Flow<GamificationState?>

    @Query("SELECT * FROM gamification_state WHERE id = 1 LIMIT 1")
    suspend fun getGameState(): GamificationState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameState(state: GamificationState)
}

@Dao
interface PostLogDao {
    @Query("SELECT * FROM post_logs ORDER BY timestamp DESC")
    fun getAllPostLogsFlow(): Flow<List<PostLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostLog(postLog: PostLogEntity)
}

@Dao
interface TriviaAnswerDao {
    @Query("SELECT * FROM user_trivia_answers ORDER BY timestamp DESC")
    fun getAllTriviaAnswersFlow(): Flow<List<TriviaAnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTriviaAnswer(answer: TriviaAnswerEntity)
}

@Dao
interface MatchmakerResultDao {
    @Query("SELECT * FROM matchmaker_results ORDER BY timestamp DESC")
    fun getAllMatchmakerResultsFlow(): Flow<List<MatchmakerResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchmakerResult(result: MatchmakerResultEntity)

    @Query("DELETE FROM matchmaker_results WHERE userEmail = :userEmail")
    suspend fun deleteResultsForUser(userEmail: String)
}

@Dao
interface SpeakerDao {
    @Query("SELECT * FROM speakers ORDER BY fullName ASC")
    fun getAllSpeakersFlow(): Flow<List<SpeakerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeakers(speakers: List<SpeakerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeaker(speaker: SpeakerEntity)

    @Query("DELETE FROM speakers WHERE id = :id")
    suspend fun deleteSpeakerById(id: String)

    @Query("DELETE FROM speakers")
    suspend fun deleteAllSpeakers()
}

@Dao
interface ExhibitorDao {
    @Query("SELECT * FROM exhibitors ORDER BY name ASC")
    fun getAllExhibitorsFlow(): Flow<List<ExhibitorEntity>>

    @Query("SELECT * FROM exhibitors LIMIT 1")
    suspend fun getAnyExhibitor(): ExhibitorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExhibitor(exhibitor: ExhibitorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExhibitors(exhibitors: List<ExhibitorEntity>)

    @Query("DELETE FROM exhibitors WHERE id = :id")
    suspend fun deleteExhibitorById(id: String)

    @Query("DELETE FROM exhibitors")
    suspend fun deleteAllExhibitors()
}

@Dao
interface AttendeeDao {
    @Query("SELECT * FROM attendees ORDER BY displayName ASC")
    fun getAllAttendeesFlow(): Flow<List<AttendeeEntity>>

    @Query("SELECT * FROM attendees LIMIT 1")
    suspend fun getAnyAttendee(): AttendeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendee(attendee: AttendeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendees(attendees: List<AttendeeEntity>)

    @Query("DELETE FROM attendees WHERE id = :id")
    suspend fun deleteAttendeeById(id: String)

    @Query("DELETE FROM attendees")
    suspend fun deleteAllAttendees()
}

@Dao
interface MatchmakerQuestionDao {
    @Query("SELECT * FROM matchmaker_questions ORDER BY id ASC")
    fun getAllQuestionsFlow(): Flow<List<MatchmakerQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: MatchmakerQuestion)

    @Query("DELETE FROM matchmaker_questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Int)
}


