package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProfileEntity::class,
        AgendaSession::class,
        BookedMeeting::class,
        ScannedContact::class,
        SocialPost::class,
        GamificationState::class,
        PostLogEntity::class,
        TriviaAnswerEntity::class,
        MatchmakerResultEntity::class,
        SpeakerEntity::class,
        ExhibitorEntity::class,
        AttendeeEntity::class,
        MatchmakerQuestion::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun agendaDao(): AgendaDao
    abstract fun meetingDao(): MeetingDao
    abstract fun contactDao(): ContactDao
    abstract fun socialPostDao(): SocialPostDao
    abstract fun gamificationDao(): GamificationDao
    abstract fun postLogDao(): PostLogDao
    abstract fun triviaAnswerDao(): TriviaAnswerDao
    abstract fun matchmakerResultDao(): MatchmakerResultDao
    abstract fun speakerDao(): SpeakerDao
    abstract fun exhibitorDao(): ExhibitorDao
    abstract fun attendeeDao(): AttendeeDao
    abstract fun matchmakerQuestionDao(): MatchmakerQuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_forward_summit_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
