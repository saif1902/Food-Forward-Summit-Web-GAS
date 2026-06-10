package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(private val db: AppDatabase) {

    private val profileDao = db.profileDao()
    private val agendaDao = db.agendaDao()
    private val meetingDao = db.meetingDao()
    private val contactDao = db.contactDao()
    private val socialPostDao = db.socialPostDao()
    private val gamificationDao = db.gamificationDao()
    private val postLogDao = db.postLogDao()
    private val triviaAnswerDao = db.triviaAnswerDao()
    private val matchmakerResultDao = db.matchmakerResultDao()
    private val speakerDao = db.speakerDao()
    private val exhibitorDao = db.exhibitorDao()
    private val attendeeDao = db.attendeeDao()
    private val matchmakerQuestionDao = db.matchmakerQuestionDao()

    val profileFlow: Flow<ProfileEntity?> = profileDao.getProfileFlow()
    val sessionsFlow: Flow<List<AgendaSession>> = agendaDao.getAllSessionsFlow()
    val meetingsFlow: Flow<List<BookedMeeting>> = meetingDao.getAllMeetingsFlow()
    val contactsFlow: Flow<List<ScannedContact>> = contactDao.getAllContactsFlow()
    val postsFlow: Flow<List<SocialPost>> = socialPostDao.getAllPostsFlow()
    val gameStateFlow: Flow<GamificationState?> = gamificationDao.getGameStateFlow()
    val postLogsFlow: Flow<List<PostLogEntity>> = postLogDao.getAllPostLogsFlow()
    val triviaAnswersFlow: Flow<List<TriviaAnswerEntity>> = triviaAnswerDao.getAllTriviaAnswersFlow()
    val matchmakerResultsFlow: Flow<List<MatchmakerResultEntity>> = matchmakerResultDao.getAllMatchmakerResultsFlow()
    val speakersFlow: Flow<List<SpeakerEntity>> = speakerDao.getAllSpeakersFlow()
    val exhibitorsFlow: Flow<List<ExhibitorEntity>> = exhibitorDao.getAllExhibitorsFlow()
    val attendeesFlow: Flow<List<AttendeeEntity>> = attendeeDao.getAllAttendeesFlow()
    val matchmakerQuestionsFlow: Flow<List<MatchmakerQuestion>> = matchmakerQuestionDao.getAllQuestionsFlow()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val matchAdapter = moshi.adapter(B2BMatchResult::class.java)

    suspend fun getProfile(): ProfileEntity? = profileDao.getProfile()
    
    suspend fun saveProfile(profile: ProfileEntity) = withContext(Dispatchers.IO) {
        profileDao.insertProfile(profile)
    }

    suspend fun toggleBookmark(id: String, isBookmarked: Boolean) = withContext(Dispatchers.IO) {
        agendaDao.updateBookmarkState(id, isBookmarked)
    }

    suspend fun bookMeeting(meeting: BookedMeeting) = withContext(Dispatchers.IO) {
        meetingDao.insertMeeting(meeting)
    }

    suspend fun cancelMeeting(id: Int) = withContext(Dispatchers.IO) {
        meetingDao.deleteMeetingById(id)
    }

    suspend fun addContact(contact: ScannedContact) = withContext(Dispatchers.IO) {
        contactDao.insertContact(contact)
    }

    suspend fun updateContactNotes(id: String, notes: String, rank: String) = withContext(Dispatchers.IO) {
        contactDao.updateContact(id, notes, rank)
    }

    suspend fun removeContact(id: String) = withContext(Dispatchers.IO) {
        contactDao.deleteContact(id)
    }

    suspend fun addSocialPost(post: SocialPost) = withContext(Dispatchers.IO) {
        socialPostDao.insertPost(post)
        postLogDao.insertPostLog(
            PostLogEntity(
                authorName = post.authorName,
                authorCompany = post.authorCompany,
                textContent = post.textContent,
                timestamp = post.timestamp
            )
        )
    }

    suspend fun deleteSocialPost(id: Int) = withContext(Dispatchers.IO) {
        socialPostDao.deletePostById(id)
    }

    suspend fun savePostLog(postLog: PostLogEntity) = withContext(Dispatchers.IO) {
        postLogDao.insertPostLog(postLog)
    }

    suspend fun saveTriviaAnswer(answer: TriviaAnswerEntity) = withContext(Dispatchers.IO) {
        triviaAnswerDao.insertTriviaAnswer(answer)
    }

    suspend fun saveMatchmakerResult(result: MatchmakerResultEntity) = withContext(Dispatchers.IO) {
        matchmakerResultDao.insertMatchmakerResult(result)
    }

    suspend fun insertSpeaker(speaker: SpeakerEntity) = withContext(Dispatchers.IO) {
        speakerDao.insertSpeakers(listOf(speaker))
    }

    suspend fun insertAgendaSession(session: AgendaSession) = withContext(Dispatchers.IO) {
        agendaDao.insertSessions(listOf(session))
    }

    suspend fun clearMatchmakerResultsForUser(userEmail: String) = withContext(Dispatchers.IO) {
        matchmakerResultDao.deleteResultsForUser(userEmail)
    }

    suspend fun toggleLikePost(post: SocialPost) = withContext(Dispatchers.IO) {
        val newLiked = !post.isLikedByMe
        val newCount = if (newLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
        socialPostDao.updateLikesState(post.id, newCount, newLiked)
    }

    suspend fun saveGameState(state: GamificationState) = withContext(Dispatchers.IO) {
        gamificationDao.insertGameState(state)
    }

    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        // Simple check for gamification state
        val currentGameState = gamificationDao.getGameState()
        if (currentGameState == null) {
            gamificationDao.insertGameState(GamificationState(id = 1))
        }

        // Add pre-populated sessions
        agendaDao.insertSessions(
            listOf(
                AgendaSession(
                    id = "session_1",
                    title = "Keynote: The Future of Lab-grown Protein Scaling",
                    speaker = "Dr. Elena Rostova",
                    speakerRole = "Chief Biotech Officer, Mycelium Frontiers",
                    startTime = "09:00 AM",
                    endTime = "10:15 AM",
                    track = "Tech & Innovation",
                    location = "Hall A (Main Stage)",
                    description = "An exploration into structural scaling barriers for alternative cultured meats and mycelium-based whole cuts. Addresses growth media cost reductions and vessel hydraulic turbulence.",
                    isBookmarked = true,
                    attachmentUrl = "Slide_Alternative_Protein_Scaling.pdf"
                ),
                AgendaSession(
                    id = "session_2",
                    title = "Panel: Seaweed & Bio-Materials in Circular Packaging",
                    speaker = "Marcus Sterling & Linda Green",
                    speakerRole = "Circular Ecology Advocates, EcoPack",
                    startTime = "10:45 AM",
                    endTime = "12:00 PM",
                    track = "Sustainability & Packaging",
                    location = "Room 204",
                    description = "Real challenges in bio-grade structural strength, shelf-life oxygen barriers, cost comparison with traditional polymers, and marine biodegradable decomposition indexes.",
                    attachmentUrl = "Ecology_Report_Seaweed_Polymers.pdf"
                ),
                AgendaSession(
                    id = "session_3",
                    title = "Masterclass: Drones & AI Imagery in Precision Farming",
                    speaker = "Captain Sean Avery",
                    speakerRole = "Director, AeroAgri Logix",
                    startTime = "01:30 PM",
                    endTime = "02:45 PM",
                    track = "Supply Chain & Automation",
                    location = "Demo Arena",
                    description = "Live demonstration of multi-spectral drone payload configurations and fast aerial analytics pipelines to optimize fertilizer delivery in nitrogen-stressed croplands.",
                    attachmentUrl = "Drones_Agri_Workbook.pdf"
                ),
                AgendaSession(
                    id = "session_4",
                    title = "Fireside: Blockchain Audits & Regulatory compliance",
                    speaker = "Sola Vance & Naomi Klein",
                    speakerRole = "Regulatory Specialists, ChocoTrace",
                    startTime = "03:15 PM",
                    endTime = "04:30 PM",
                    track = "Consumer & Regulatory",
                    location = "East Pavilion",
                    description = "Exploring global chocolate deforestation compliance policies and how decentralized geofence smart contracts can prove compliance under strict EU supply rules."
                )
            )
        )

        // Seed social feed
        val seedPost1 = SocialPost(
            id = 1,
            authorName = "Sophia Weiss",
            authorCompany = "Grain Millers Inc.",
            textContent = "Who is joining my fireside chat on sustainable grain sourcing and climate-resilient proteins at 2 PM today? Would love to share our regional feedback!",
            likesCount = 8,
            timestamp = System.currentTimeMillis() - 7200000,
            authorRole = "Speaker"
        )
        val seedPost2 = SocialPost(
            id = 2,
            authorName = "BioCult Agri-Labs",
            authorCompany = "Booth B-02, Sourcing Hall",
            textContent = "Our bioreactor is fully live! Stop by and pitch your ideas for alternative seaweed-protein scaffolds. We has specialized samples ready.",
            likesCount = 15,
            timestamp = System.currentTimeMillis() - 3600000,
            authorRole = "Exhibitor"
        )
        socialPostDao.insertPost(seedPost1)
        socialPostDao.insertPost(seedPost2)

        postLogDao.insertPostLog(
            PostLogEntity(
                id = 1,
                authorName = seedPost1.authorName,
                authorCompany = seedPost1.authorCompany,
                textContent = seedPost1.textContent,
                timestamp = seedPost1.timestamp
            )
        )
        postLogDao.insertPostLog(
            PostLogEntity(
                id = 2,
                authorName = seedPost2.authorName,
                authorCompany = seedPost2.authorCompany,
                textContent = seedPost2.textContent,
                timestamp = seedPost2.timestamp
            )
        )

        val seedSpeakers = listOf(
            SpeakerEntity(
                id = "884384d3-2093-41e2-8b7f-8299e0ba4615",
                createdAt = "2026-04-28 09:20:36.498008+00",
                fullName = "Alexande Kappes",
                email = "a.m.kappes@gmail.com",
                topicTitle = "AI at the Fence Line: What Livestock Technology Is Teaching the Rest of the Food System About Deploying AI to Farmers & Ranchers",
                bio = "As founder of Greener Herd, I built a digital livestock management platform serving smallholder farmers across the Middle East and Africa — regions where animal agriculture sustains hundreds of millions of livelihoods but has been almost entirely overlooked by mainstream agtech. In partnership with the UN World Food Programme and FAO, Greener Herd brings basic digital infrastructure, animal records, fertility and mortality tracking, nutrition guidance, to operations that have never had it. My contribution to food systems innovation is closing the data gap at the base of the global animal protein pyramid, where the productivity gains are largest and the tools have been thinnest.",
                sessionFormat = "Panel Discussion",
                avRequirements = "",
                submissionType = "propose_panel",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "https://www.linkedin.com/in/alexander-m-kappes/",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "ad67408c-15ad-4164-b4f0-45a68dbba69d",
                createdAt = "2026-04-28 14:48:32.358737+00",
                fullName = "ASHLEY NICHOLLS",
                email = "ashley@reachag.ca",
                topicTitle = "Boots On The Ground Innovation",
                bio = "Through my work with REACH Agriculture Strategies, I focus on a critical but often overlooked driver of food systems innovation: people. Specifically, the frontline workforce that operates within our farms, feedlots, and agricultural businesses every day. While much of the innovation conversation centers on technology, sustainability metrics, and policy, my work emphasizes that lasting innovation only occurs when it is understood, and adopted by the people closest to the work.\n\nAt REACH, I work with agricultural operations to develop human management systems that effect the attraction, retention and effectiveness of employees. This workforce development becomes a lever for both economic and environmental performance in operations.\n\nIn collaboration with organizations such as the Calgary Stampede and Ag for Life, I have contributed to broader food system engagement by helping translate agriculture to non-ag audiences and strengthening the connection between producers and the public. These platforms play a key role in building trust, improving transparency, and supporting the social license required for a resilient food system.\n\nMy keynote work builds on these experiences, challenging audiences to rethink innovation not as something that happens at the leadership or policy level alone, but as something that must be co-created with and carried by frontline teams. By drawing parallels between low-stress livestock handling and human leadership, as well as exploring the full lifecycle of an agricultural employee, I provide practical frameworks that help organizations align people, performance, and purpose.\n\nUltimately, my contribution to food systems innovation lies in bridging the gap between strategy and execution. By bringing “boots on the ground” perspectives into conversations about sustainability and innovation, I aim to ensure that the future of food is not only designed well, but also delivered effectively.",
                sessionFormat = "Keynote",
                avRequirements = "",
                submissionType = "pitch_talk",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "https://www.linkedin.com/in/ashley-nicholls-/",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "b8177664-818a-451b-a9b0-50522b1daa2b",
                createdAt = "2026-04-29 00:01:20.47657+00",
                fullName = "Saif",
                email = "saif.ahmed@nleats.com",
                topicTitle = "Agritech",
                bio = "Test",
                sessionFormat = "Keynote",
                avRequirements = "Test",
                submissionType = "pitch_talk",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "https://ca.linkedin.com/in/saifahmed-",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "ba34ffa2-9653-467f-be17-d52d57024f9c",
                createdAt = "2026-05-02 19:12:44.750015+00",
                fullName = "Heidi M. Peterson",
                email = "hpeterson@sandcountyfoundation.org",
                topicTitle = "Soil health/Regenerative Agriculture",
                bio = "Sand County Foundation (SCF) is a 501(c)(3) non-profit conservation organization whose work with farmers and ranchers 60 years has grown through inspiration, innovation, and investment. SCF’s mission is to inspire and empower private land managers to ethically manage natural resources in their care so future generations have clean and abundant water, healthy soil to support agriculture and forestry, plentiful habitat for wildlife and opportunities for outdoor recreation. SCF helps private landowners achieve their full potential as conservationists by providing public recognition for outstanding private land stewardship; facilitating the exchange of information among landowners, scientists, funders and policy makers; and creating landowner-led examples of environmental improvement suitable for replication. \nDr. Heidi M. Peterson, VP Agricultural Research & Conservation, sets Sand County Foundation's strategic direction in research, and farmer and rancher engagement. She brings significant leadership, teaching, and research experience surrounding agricultural conservation and water quality issues. Over her past seven years with SCF, she launched the Land Ethic Mentorship for historically underserved farmers, initiated a peer-to-peer on-farm soil health demonstration with 116 farmers across six states, and supported the development of a grassland initiative targeting regenerative management over one million acres. Heidi previously served as the Phosphorus Program Director with the International Plant Nutrition Institute, and prior to that at the Minnesota Department of Agriculture. She serves the scientific community on the Field to Market's Science Advisory Council, Foundation for Food & Agricultural Research's Advisory Council, Agronomic Science Foundation's Board of Trustees, as an adjunct professor at the University of Minnesota, and as a technical editor with the Journal of Environmental Quality.",
                sessionFormat = "Keynote",
                avRequirements = "*Happy to serve as a panelist, keynote or fireside chat.",
                submissionType = "pitch_talk",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "https://www.linkedin.com/in/heidi-peterson-5a931648/",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "9042fdd6-2d4b-48ea-b991-40730d3731aa",
                createdAt = "2026-05-04 15:23:11.928926+00",
                fullName = "Sophia Weiss",
                email = "sophia.weiss@grainmillers.com",
                topicTitle = "Scaling regenerative agriculture across the Canadian Prairies and building vibrant cross sector partnerships",
                bio = "Sophia Weiss is the Director of Sustainability and Regenerative Agriculture at Grain Millers Inc., one of the largest oat processors in North America. She holds a master’s degree in Agriculture, Food, and Environment from the Friedman School of Nutrition Science and Policy at Tufts University and is a registered dietitian. Sophia brings a multidisciplinary lens to her work, bridging agronomy, corporate responsibility, and public health to advance sustainability across the agricultural supply chain. At Grain Millers, she leads the company’s sustainability strategy, including regenerative agriculture programming, facility-level environmental initiatives, carbon accounting, and ESG reporting. She works closely with internal teams and external partners to implement climate-smart practices, reduce greenhouse gas emissions, and promote long-term soil health on farms. Her efforts focus on building transparent and resilient systems that support both environmental outcomes and farmer prosperity.",
                sessionFormat = "Fireside Chat",
                avRequirements = "",
                submissionType = "propose_panel",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "https://www.linkedin.com/in/sophia-weiss/",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "a7fa3320-31be-4c0f-a682-6d2d6b513551",
                createdAt = "2026-05-05 14:33:59.087472+00",
                fullName = "Marc-André Roberge",
                email = "marc@nectar.buzz",
                topicTitle = "Leveraging agritech to benefit nature",
                bio = "With a background in product design, Marc-André developed a passion for beekeeping while working on designs to improve human-bees interactions. He's CEO and Co-founder at Nectar, a company on a mission to help beekeepers raise thriving honey bee colonies to secure our food supply.",
                sessionFormat = "Keynote",
                avRequirements = "Screen to display deck",
                submissionType = "pitch_talk",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "https://www.linkedin.com/in/marcandreroberge/",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "97303fd5-bab6-47b3-b668-86de10442635",
                createdAt = "2026-05-05 14:40:39.446351+00",
                fullName = "Maha Tahiri",
                email = "maha@s2bgroup.com",
                topicTitle = "Food Forward in the GLP-1 Era: Reinventing Food Systems for a New Consumer-Patient",
                bio = "Maha Tahiri is a globally recognized nutrition strategist with three decades of experience translating food science into business strategy and food systems transformation. As Co-Founder and CEO of S2B Group, she advises Fortune 300 CPG companies, ingredient innovators, and commodity boards on aligning nutrition science, consumer behavior, and sustainability.\nMaha is the architect and convenor of the GLP-1 Nutrition Profiles Collaborative, a first-of-its-kind precompetitive consortium setting science-based standards for the 100+ million consumers projected to use GLP-1 therapies by decade's end. The coalition spans Big CPG, small and mid-sized innovators, ingredient suppliers, and commodity boards. Under her leadership, it is defining nutrient density benchmarks, protein and fiber thresholds, and product development guardrails that will shape category innovation, retail merchandising, and public health guidance for a generation of consumer-patients.\nHer work spans the green and blue food economy, advising on precision fermentation proteins, regenerative dairy, sustainable hydration science, and the repositioning of nutrient-dense proteins for an aging, medicated, climate-conscious population. She authored the consumer-patient identity framework now informing how food companies, regulators, and investors think about the convergence of pharma and food.\nPreviously Chief Health and Wellness Officer at General Mills, Maha serves on the American Society for Nutrition's Strategic Oversight Committee and multiple advisory boards. She has presented at Future Food Tech, Food Ingredients Europe, IFT First, IDFA, McCormick Global Consumer Day, and forums convened by Nestlé MENA, Houlihan Lokey, and HEC Paris, and speaks regularly on global nutrition policy across the US, UK, France, Chile, Mexico, Brazil, Indonesia, and Canada.\nMaha holds a PhD in nutrition, is Adjunct Professor at Tufts University, and is professionally fluent in English, French, Arabic, and Spanish.",
                sessionFormat = "Keynote",
                avRequirements = "",
                submissionType = "pitch_talk",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "https://www.linkedin.com/in/mahatahiri/",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "206de90c-c5ac-4d84-855a-af7e149bb001",
                createdAt = "2026-05-08 18:43:36.35489+00",
                fullName = "Daniela Galloro",
                email = "marketing@biomemakers.com",
                topicTitle = "Agritech, Regenerative Farming, AI in Food Systems, Supply Chain Resilience, Sustainability Metrics. Beyond the Checklist: Leveraging AI to Verify Soil Health and Supply Chain Resilience",
                bio = "Dr. Jacob Parnell is a globally recognized authority in soil microbiology and sustainable agriculture, currently serving as the Director of Agronomy at Biome Makers. With a Ph.D. from Michigan State University’s Center for Microbial Ecology, Dr. Parnell has spent over two decades bridging the gap between microbial science and commercial agricultural viability.\n\nHis career spans pivotal roles at the United Nations Food and Agriculture Organization (FAO), where he led the development of a global soil biodiversity observatory, and the National Ecological Observatory Network (NEON), where he designed terrestrial microbial strategies. At Biome Makers, he leverages BeCrop® technology and AI-driven data to translate complex biological datasets into actionable insights for the world’s largest food producers.",
                sessionFormat = "Keynote",
                avRequirements = "microphone, screensharing",
                submissionType = "nominate_speaker",
                nomineeName = "Dr. Jacob Parnell",
                nomineeEmail = "jacob.parnell@biomemakers.com",
                linkedinUrl = "https://www.linkedin.com/in/jjacob-parnell/",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "be5c927d-2c3c-48c0-b724-e966dc79ee08",
                createdAt = "2026-05-14 13:17:56.630925+00",
                fullName = "Mehnaz Tabassum Mehnaz Tabassum",
                email = "mehnaz.tabassum@nleats.com",
                topicTitle = "sd",
                bio = "sdsds",
                sessionFormat = "Keynote",
                avRequirements = "sdd",
                submissionType = "pitch_talk",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "dsd",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "94b47f3f-7333-431f-880a-4b377af8c824",
                createdAt = "2026-05-20 00:02:52.416437+00",
                fullName = "Brendon Steele",
                email = "bsteele@future500.org",
                topicTitle = "Food System & Supply Chain Sustainability",
                bio = "I received an email from Melissa Sewcharran suggesting my organization, Future 500, submit a speaker proposal. Our initial thought is to convene together a small panel or large 'fireside chat' with an NGO we respect and a large company they engage with to explore effective corporate/civil society collaboration to advance sustainable food value chains across the globe—exploring the intersection of nature, supply chains, and building community trust—with us at Future 500 as the moderator or potential panelist. \n\nI saw through the FAQs through the link above that we don't need to confirm panelists before submission, and that instead we can outline the types of contributions we would like—so I have not yet reached out. The person I'd first ask is Glenn Hurowitz at Mighty Earth, an NGO campaigner we highly respect and find to be impactful, effective, nuanced, and thinking through systems change, especially on food commodity supply chains. I'd like to ask him if there's a company he could recommend who could have an on-stage discussion like this (as not every company would be open to a public discussion).\n\nIf we have trouble securing a corporate voice, this could be a 'fireside chat' with Glenn or a colleague. If Mighty Earth is not interested (they might need travel costs reimbursed, for example) we could approach others. \n\nIf we were to broaden the topic to effective Corporate Nature Strategy (beyond just food supply chains) there are several others I have top-of-mind we could approach. Similar on the topic of oceans (blue economy).\n\nAnd we at Future 500 could propose other topic areas. I saw AI referenced here and I'm involved in moderating a session soon on effective AI use in corporate sustainability for food companies, especially anticipating the trajectory of how AI will be rapidly advancing.\n\nI've kept this a little loose to keep some ideas open for exploration, and welcome feedback on if there's a better fit.",
                sessionFormat = "Panel Discussion",
                avRequirements = "",
                submissionType = "propose_panel",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "https://www.linkedin.com/in/brendonsteele",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "5feef27b-db94-426c-ad9b-4b43f92c5623",
                createdAt = "2026-05-21 16:46:46.092467+00",
                fullName = "Carin Gerhardt",
                email = "carin.gerhardt@svgventures.com",
                topicTitle = "Scaling Access to Sustainable & Nutritious Food Through Partnerships",
                bio = "Carin Gerhardt is Director of Corporate Programs at THRIVE by SVG Ventures, where she leads global innovation partnerships connecting startups, corporations, investors, and research organizations across agrifood, climate, AI, and deep technology ecosystems.\n\nOriginally from Brazil, Carin has spent her career driving innovation in food and agriculture — from food science research and applied R&D to accelerator programs, corporate innovation, venture collaboration, and international ecosystem development. She has helped design and accelerate open innovation programs within large multinational organizations, fostering strategic partnerships and commercialization opportunities between startups and industry leaders.\n\nAt THRIVE, Carin has played a key role in strengthening collaboration across global innovation ecosystems, helping connect emerging technology companies with corporate partners, investors, and research institutions across Silicon Valley, Latin America, Japan, and beyond. Passionate about bringing people together across sectors, she focuses on advancing innovation that addresses critical cross-industry challenges and contributes to building a more sustainable, resilient, and equitable food system.",
                sessionFormat = "Panel Discussion",
                avRequirements = "",
                submissionType = "propose_panel",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "https://www.linkedin.com/in/caringerhardt/",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "33f4effc-14e6-4774-b2ea-0fc72d07c670",
                createdAt = "2026-05-29 16:37:02.982494+00",
                fullName = "Julie Francoeur",
                email = "communications@fairtrade.ca",
                topicTitle = "“From Risk to Relationship: Why Business Models Built on Equity are Outperforming in Uncertain Markets” . Companies that treat sourcing as a partnership rather than as a transaction are better positioned for success when global food systems are under pressure.",
                bio = "Julie Francoeur is the CEO of Fairtrade Canada and a member of the Fairtrade International Executive Team. She also serves on the Board of Directors of Cooperation Canada and the Sustainability Committee of the Coffee Association of Canada.  \n\nFor over two decades, Julie has worked with farmer organizations, global brands, and governments around the world to improve incomes, strengthen resilience, and reduce long-term supply risk.   \n\nJulie works with businesses and policymakers on commercially viable models that deliver sustainable livelihoods for farmers and workers. Her focus is on translating concepts such as living income, responsible procurement, and due diligence into practical strategies that companies can implement at scale.  \n\nUnder Julie’s leadership, Fairtrade-certified sales in Canada have grown to over $850 million annually, reflecting the growing demand for more transparent and resilient supply chains. At a moment when climate shocks, regulatory pressure, and supply disruption are exposing the fragility of conventional sourcing models, Julie encourages us to think of ethical trade as a sound strategic choice.",
                sessionFormat = "Keynote",
                avRequirements = "TBD",
                submissionType = "nominate_speaker",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "https://www.linkedin.com/in/francoeurjulie/",
                location = "Toronto, ON"
            ),
            SpeakerEntity(
                id = "89302393-6920-41df-a3b6-61ad346830c2",
                createdAt = "2026-06-08 13:02:09.087013+00",
                fullName = "Meifan Shi",
                email = "meifan@waterpointlane.com",
                topicTitle = "AI",
                bio = "What I do every day is back the companies that will actually change the food system, not iterate on it. Building Waterpoint Lane from the ground up means deploying capital at the frontier of what's possible, not what's proven. That's led us to Relocalize, a Quebec company using AI and autonomous micro-factories to eliminate up to 90% of supply chain emissions; Verdi Ag, a Vancouver company that saved over 100 million liters of water in a single year through smart irrigation automation; and Heritable, a Google X spinout making plants programmable through AI and genomics, compressing decades of crop improvement into years. These aren't improvements to the old system. They are the new system. And we built this firm around the conviction that Canada sits at the center of this transformation: the land, the water, the science, and the capital to lead it.\nHere are 3 suggestions for keynotes: \n1) The Invisible Infrastructure The AI reshaping food won't show up on your plate, it lives in the soil sensor, the supply chain algorithm, and the model linking what you ate to how you'll feel in ten years. What cloud did to enterprise software, AI is doing to food.\n2) Food Is Medicine. AI Is the Prescription. GLP-1 drugs are redirecting billions toward whole, real food. Longevity science is making the diet-lifespan link undeniable. Drawing on our portfolio and work with Ontario Genomics, this talk connects Canada's food policy ambitions to the global health economy.  The country that wins the food-as-medicine race wins both its public health crisis and its next great export market.\n3) Betting on Food's Future (Fireside) A candid conversation about where global capital is flowing, what AI-driven founders are actually building, and why water and food security are converging into the defining infrastructure challenge of our generation. Less like a talk, more like a front-row seat to the future.",
                sessionFormat = "Keynote",
                avRequirements = "",
                submissionType = "pitch_talk",
                nomineeName = "",
                nomineeEmail = "",
                linkedinUrl = "https://www.linkedin.com/in/meifanshi/",
                location = "Toronto, ON"
            )
        )
        speakerDao.insertSpeakers(seedSpeakers)

        // Seed exhibitors if empty
        if (exhibitorDao.getAnyExhibitor() == null) {
            val seedExhibitors = ExhibitorsData.list.map { ex ->
                ExhibitorEntity(
                    id = ex.id,
                    name = ex.name,
                    focus = ex.focus,
                    track = ex.track,
                    description = ex.description,
                    boothLocation = ex.boothLocation,
                    website = ex.website,
                    contactEmail = ex.contactEmail,
                    tier = ex.tier,
                    logoAsset = ex.logoAsset
                )
            }
            exhibitorDao.insertExhibitors(seedExhibitors)
        }

        // Seed attendees if empty
        if (attendeeDao.getAnyAttendee() == null) {
            val seedAttendees = CSVProfileData.list.map { csv ->
                AttendeeEntity(
                    id = csv.id,
                    displayName = csv.displayName,
                    companyDescription = csv.companyDescription,
                    websiteUrl = csv.websiteUrl,
                    linkedinUrl = csv.linkedinUrl,
                    countryRegion = csv.countryRegion,
                    annualRevenue = csv.annualRevenue,
                    currentMarkets = csv.currentMarkets,
                    targetMarkets = csv.targetMarkets,
                    importExportStatus = csv.importExportStatus,
                    brandsRepresented = csv.brandsRepresented,
                    primarySectors = csv.primarySectors,
                    targetBuyers = csv.targetBuyers,
                    boothSizeConfirmed = csv.boothSizeConfirmed,
                    electricalNeeds = csv.electricalNeeds,
                    exhibitorLeadId = csv.exhibitorLeadId,
                    email = if (csv.id == "feb8a00c-839e-4412-80c8-2e76765a1014") "cognico@nleats.com" else csv.id.take(8) + "@ffsummit.com",
                    role = if (csv.id == "feb8a00c-839e-4412-80c8-2e76765a1014") "Sponsor" else "Attendee"
                )
            }
            attendeeDao.insertAttendees(seedAttendees)
        }
    }

    // Speakers CRUD
    suspend fun addOrUpdateSpeaker(speaker: SpeakerEntity) = withContext(Dispatchers.IO) {
        speakerDao.insertSpeaker(speaker)
    }

    suspend fun deleteSpeaker(id: String) = withContext(Dispatchers.IO) {
        speakerDao.deleteSpeakerById(id)
    }

    // Exhibitors CRUD
    suspend fun addOrUpdateExhibitor(exhibitor: ExhibitorEntity) = withContext(Dispatchers.IO) {
        exhibitorDao.insertExhibitor(exhibitor)
    }

    suspend fun deleteExhibitor(id: String) = withContext(Dispatchers.IO) {
        exhibitorDao.deleteExhibitorById(id)
    }

    // Attendees CRUD
    suspend fun addOrUpdateAttendee(attendee: AttendeeEntity) = withContext(Dispatchers.IO) {
        attendeeDao.insertAttendee(attendee)
    }

    suspend fun deleteAttendee(id: String) = withContext(Dispatchers.IO) {
        attendeeDao.deleteAttendeeById(id)
    }

    suspend fun generateB2BMatch(
        name: String,
        company: String,
        goal: String,
        track: String,
        tier: String
    ): B2BMatchResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.trim().isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("AppRepository", "API Key is template placeholder, trigger local fallback")
            return@withContext handleLocalFallbackSave(name, company, goal, track, tier)
        }

        val exhibitorsInfo = ExhibitorsData.list.joinToString("\n") { ex ->
            "ID: ${ex.id}, Name: ${ex.name}, Focus: ${ex.focus}, Track: ${ex.track}, Details: ${ex.description}"
        }

        val prompt = """
            You are an advanced enterprise B2B matchmaking agent running inside a mobile event app's backend for the Food Forward Summit 2026.
            Your objective is to ingest an individual attendee's profile/onboarding answers and cross-reference them against an array of available event exhibitors. You must select the top 3 most synergetic matches and generate a highly personalized welcome greeting.

            Here are the attendee onboarding profile answers:
            - Name: $name
            - Company: $company
            - Objective: $goal
            - Preferred Track: $track
            - Budget/Tier Goal: $tier

            Here is the database of exhibitors at the Food Forward Summit 2026:
            $exhibitorsInfo

            CRITICAL RULES:
            1. You must only return a valid, strictly structured JSON object. 
            2. Do not include markdown code blocks (like ```json ... ```) in the raw API response.
            3. Your JSON structure must match this exact schema:
            {
              "welcomeMessage": "A concise, engaging 2-sentence greeting mentioning their specific goal.",
              "primaryTrackRecommended": "The exact name of the track matching their profile",
              "recommendedExhibitors": [
                {
                  "exhibitorId": "String matching the database primary key ID provided",
                  "exhibitorName": "String",
                  "matchScore": 1-100 integer based on dataset relevance,
                  "matchReason": "A 1-sentence explanation of why this specific company benefits their business objectives."
                }
              ]
            }
        """.trimIndent()

        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                generationConfig = GeminiGenerationConfig(
                    responseFormat = ResponseFormat(text = ResponseFormatText(mimeType = "application/json")),
                    temperature = 0.4f
                )
            )

            val rawResponse = RetrofitClient.service.generateContent(apiKey, request)
            val rawText = rawResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleanText = cleanJson(rawText)
            
            val result = matchAdapter.fromJson(cleanText) ?: throw Exception("JSON parse returned null")
            
            val existing = profileDao.getProfile()
            // Save results to database profile
            val currentProfile = ProfileEntity(
                id = 1,
                name = name,
                company = company,
                businessGoal = goal,
                preferredTrack = track,
                budgetTier = tier,
                matchedJson = cleanText,
                email = existing?.email ?: "",
                provider = existing?.provider ?: "",
                isOtpVerified = existing?.isOtpVerified ?: true,
                role = existing?.role ?: "Attendee",
                isOnboarded = existing?.isOnboarded ?: true,
                countryRegion = existing?.countryRegion ?: "",
                websiteUrl = existing?.websiteUrl ?: "",
                linkedinUrl = existing?.linkedinUrl ?: "",
                instagramUrl = existing?.instagramUrl ?: "",
                xUrl = existing?.xUrl ?: "",
                annualRevenue = existing?.annualRevenue ?: "",
                currentMarkets = existing?.currentMarkets ?: "",
                targetMarkets = existing?.targetMarkets ?: "",
                importExportStatus = existing?.importExportStatus ?: "",
                brandsRepresented = existing?.brandsRepresented ?: "",
                primarySectors = existing?.primarySectors ?: "",
                targetBuyers = existing?.targetBuyers ?: "",
                boothSizeConfirmed = existing?.boothSizeConfirmed ?: "",
                electricalNeeds = existing?.electricalNeeds ?: "",
                exhibitorLeadId = existing?.exhibitorLeadId ?: "",
                uniqueId = existing?.uniqueId ?: ""
            )
            profileDao.insertProfile(currentProfile)
            
            // Save to matchmaker_results table
            saveMatchmakerResultsToDb(currentProfile.email.ifEmpty { "anonymous@example.com" }, result)
            
            result
        } catch (e: Exception) {
            Log.e("AppRepository", "Gemini API error, falling back to local matches", e)
            handleLocalFallbackSave(name, company, goal, track, tier)
        }
    }

    private suspend fun handleLocalFallbackSave(
        name: String,
        company: String,
        goal: String,
        track: String,
        tier: String
    ): B2BMatchResult {
        val result = runLocalMatchmakingFallback(name, company, goal, track, tier)
        val jsonText = matchAdapter.toJson(result)
        val existing = profileDao.getProfile()
        val profileToSave = ProfileEntity(
            id = 1,
            name = name,
            company = company,
            businessGoal = goal,
            preferredTrack = track,
            budgetTier = tier,
            matchedJson = jsonText,
            email = existing?.email ?: "",
            provider = existing?.provider ?: "",
            isOtpVerified = existing?.isOtpVerified ?: true,
            role = existing?.role ?: "Attendee",
            isOnboarded = existing?.isOnboarded ?: true,
            countryRegion = existing?.countryRegion ?: "",
            websiteUrl = existing?.websiteUrl ?: "",
            linkedinUrl = existing?.linkedinUrl ?: "",
            instagramUrl = existing?.instagramUrl ?: "",
            xUrl = existing?.xUrl ?: "",
            annualRevenue = existing?.annualRevenue ?: "",
            currentMarkets = existing?.currentMarkets ?: "",
            targetMarkets = existing?.targetMarkets ?: "",
            importExportStatus = existing?.importExportStatus ?: "",
            brandsRepresented = existing?.brandsRepresented ?: "",
            primarySectors = existing?.primarySectors ?: "",
            targetBuyers = existing?.targetBuyers ?: "",
            boothSizeConfirmed = existing?.boothSizeConfirmed ?: "",
            electricalNeeds = existing?.electricalNeeds ?: "",
            exhibitorLeadId = existing?.exhibitorLeadId ?: "",
            uniqueId = existing?.uniqueId ?: ""
        )
        profileDao.insertProfile(profileToSave)
        
        // Save to matchmaker_results table
        saveMatchmakerResultsToDb(profileToSave.email.ifEmpty { "anonymous@example.com" }, result)
        
        return result
    }

    private suspend fun saveMatchmakerResultsToDb(userEmail: String, result: B2BMatchResult) {
        matchmakerResultDao.deleteResultsForUser(userEmail)
        result.recommendedExhibitors.forEach { ex ->
            matchmakerResultDao.insertMatchmakerResult(
                MatchmakerResultEntity(
                    userEmail = userEmail,
                    exhibitorId = ex.exhibitorId,
                    contentTitle = ex.exhibitorName,
                    contentDescription = "Reason: ${ex.matchReason}"
                )
            )
        }
    }

    private fun runLocalMatchmakingFallback(
        name: String,
        company: String,
        goal: String,
        track: String,
        tier: String
    ): B2BMatchResult {
        // Let's analyze user preferences and generate a personalized welcome message and top 3 exhibitors
        val welcoming = "Hi $name from $company! Welcome to the Food Forward Summit. Based on your objective to $goal, we have mapped out a tailored experience across the event."
        
        // Find best matches
        val sortedExhibitors = ExhibitorsData.list.map { ex ->
            var score = 50
            if (ex.track.lowercase() == track.lowercase()) {
                score += 30
            }
            
            val goalWords = goal.lowercase().split(" ", ",", ".")
            val matchWordsCount = goalWords.count { word ->
                word.length > 3 && (
                    ex.description.lowercase().contains(word) ||
                    ex.focus.lowercase().contains(word) ||
                    ex.name.lowercase().contains(word)
                )
            }
            score += minOf(20, matchWordsCount * 5)
            
            if (ex.tier == "Platinum") score += 5
            
            RecommendedExhibitor(
                exhibitorId = ex.id,
                exhibitorName = ex.name,
                matchScore = minOf(100, score),
                matchReason = when (ex.id) {
                    "ex_biocult" -> "They offer cellular reactor blueprints perfectly aligned with scaling raw cellular agriculture."
                    "ex_ecopack" -> "Their seaweed composites perfectly answer sustainable, bio-degradable wrapping initiatives."
                    "ex_agridrone" -> "Their autonomous flight vector logistics will optimize precise compound deployment in your supply chain."
                    "ex_verdevertical" -> "Their high-yield metropolitan circular kits bypass logistics bottlenecks for urban cultivation."
                    "ex_freezefresh" -> "Their liquid-nitrogen cold shipping technology ensures safe organic protein freight storage."
                    "ex_mycelium" -> "Their solid whole-cut mushroom meat brewing provides excellent sustainable retail inventory."
                    "ex_aqualoop" -> "Their recirculating marine technology minimizes eco-footprint for coastal harvest networks."
                    "ex_chocotrace" -> "Their decentralized web platform provides bulletproof geofence and compliant compliance trails."
                    else -> "Their innovative solutions provide key synergy with your goals."
                }
            )
        }.sortedByDescending { it.matchScore }.take(3)

        return B2BMatchResult(
            welcomeMessage = welcoming,
            primaryTrackRecommended = track,
            recommendedExhibitors = sortedExhibitors
        )
    }

    private fun cleanJson(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.substringAfter("```json")
        } else if (text.startsWith("```")) {
            text = text.substringAfter("```")
        }
        if (text.endsWith("```")) {
            text = text.substringBeforeLast("```")
        }
        return text.trim()
    }

    suspend fun insertMatchmakerQuestion(question: MatchmakerQuestion) = withContext(Dispatchers.IO) {
        matchmakerQuestionDao.insertQuestion(question)
    }

    suspend fun deleteMatchmakerQuestion(id: Int) = withContext(Dispatchers.IO) {
        matchmakerQuestionDao.deleteQuestionById(id)
    }
}
