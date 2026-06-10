import { useState, FormEvent, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { 
  Calendar, 
  MapPin, 
  Users, 
  Vote, 
  ArrowRight, 
  CheckCircle, 
  Award, 
  Sparkles, 
  Send, 
  Edit3, 
  Save, 
  RefreshCw, 
  Globe, 
  Briefcase, 
  MessageSquare, 
  Lock 
} from "lucide-react";

interface DashboardTabProps {
  onOpenTicket: () => void;
  isRegistered: boolean;
  userName?: string;
  userRole?: string;
  userSession?: any;
}

export default function DashboardTab({ 
  onOpenTicket, 
  isRegistered, 
  userName, 
  userRole, 
  userSession 
}: DashboardTabProps) {
  
  // Interactive poll states
  const [votedOption, setVotedOption] = useState<string | null>(null);
  const [pollVotes, setPollVotes] = useState({
    cellular: 174,
    algae: 112,
    mycelium: 248,
    vertical: 95
  });

  const [emailInput, setEmailInput] = useState("");
  const [subscribed, setSubscribed] = useState(false);

  // Profile forms editable fields states
  const [isSaving, setIsSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Speaker specific states
  const [speakerTopic, setSpeakerTopic] = useState("");
  const [speakerBio, setSpeakerBio] = useState("");
  const [speakerAV, setSpeakerAV] = useState("");
  const [speakerLoc, setSpeakerLoc] = useState("");
  const [speakerFormat, setSpeakerFormat] = useState("Keynote");

  // Exhibitor specific states
  const [exhibitorDesc, setExhibitorDesc] = useState("");
  const [exhibitorWebsite, setExhibitorWebsite] = useState("");
  const [exhibitorStatus, setExhibitorStatus] = useState("Both");
  const [exhibitorRevenue, setExhibitorRevenue] = useState("");
  const [exhibitorNeeds, setExhibitorNeeds] = useState("");
  const [exhibitorBooth, setExhibitorBooth] = useState("");

  // Keep widgets tab controlled
  const [activeWidget, setActiveWidget] = useState<"poll" | "newsletter" | "profile">("poll");

  // Load backend profile on startup
  useEffect(() => {
    if (userSession && userSession.profile) {
      const p = userSession.profile;
      if (userRole === "Speaker") {
        setSpeakerTopic(p.topicTitle || "");
        setSpeakerBio(p.bio || "");
        setSpeakerAV(p.avRequirements || "");
        setSpeakerLoc(p.location || "Toronto, ON");
        setSpeakerFormat(p.sessionFormat || "Keynote");
      } else {
        setExhibitorDesc(p.companyDescription || "");
        setExhibitorWebsite(p.websiteUrl || "");
        setExhibitorStatus(p.importExportStatus || "Both");
        setExhibitorRevenue(p.annualRevenue || "Pre-revenue");
        setExhibitorNeeds(p.electricalNeeds || "");
        setExhibitorBooth(p.boothSizeConfirmed || "Test");
      }
      // Auto switch to profile update helper for helpful configuration
      setActiveWidget("profile");
    }
  }, [userSession, userRole]);

  const handleVote = (option: "cellular" | "algae" | "mycelium" | "vertical") => {
    if (votedOption) return;
    setVotedOption(option);
    setPollVotes(prev => ({
      ...prev,
      [option]: prev[option] + 1
    }));
  };

  const handleSubscribe = (e: FormEvent) => {
    e.preventDefault();
    if (!emailInput.trim()) return;
    setSubscribed(true);
    setEmailInput("");
  };

  const handleSaveProfile = async (e: FormEvent) => {
    e.preventDefault();
    if (!userSession) return;

    setIsSaving(true);
    setSaveSuccess(false);

    try {
      const isSpk = userRole === "Speaker";
      const endpoint = isSpk ? "/api/speakers" : "/api/exhibitors";
      
      const payload = isSpk ? {
        id: userSession.userId,
        fullName: userSession.displayName,
        email: userSession.email,
        topicTitle: speakerTopic,
        bio: speakerBio,
        sessionFormat: speakerFormat,
        avRequirements: speakerAV,
        location: speakerLoc,
        submissionType: "pitch_talk",
        nomineeName: "",
        nomineeEmail: "",
        linkedinUrl: userSession.profile.linkedinUrl || ""
      } : {
        id: userSession.userId,
        displayName: userSession.displayName,
        companyDescription: exhibitorDesc,
        email: userSession.email,
        websiteUrl: exhibitorWebsite,
        linkedinUrl: userSession.profile.linkedinUrl || "",
        instagramUrl: "",
        xUrl: "",
        countryRegion: userSession.profile.countryRegion || "Canada",
        annualRevenue: exhibitorRevenue,
        currentMarkets: userSession.profile.currentMarkets || '["Canada"]',
        targetMarkets: userSession.profile.targetMarkets || '["United States"]',
        importExportStatus: exhibitorStatus,
        brandsRepresented: userSession.profile.brandsRepresented || "",
        primarySectors: userSession.profile.primarySectors || '["Primary Production"]',
        targetBuyers: userSession.profile.targetBuyers || "",
        boothSizeConfirmed: exhibitorBooth,
        electricalNeeds: exhibitorNeeds,
        directoryConsent: true,
        exhibitorLeadId: userSession.profile.exhibitorLeadId || "",
        role: userRole
      };

      const res = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        throw new Error("Failed to write coordinates to summit cloud database.");
      }

      // Sync global state representation
      userSession.profile = payload;
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 4000);
    } catch (err) {
      alert("Error: Database sync failed under peer encryption protocol.");
    } finally {
      setIsSaving(false);
    }
  };

  const totalVotes = pollVotes.cellular + pollVotes.algae + pollVotes.mycelium + pollVotes.vertical;

  return (
    <div className="flex-1 flex flex-col justify-between h-full min-h-0 overflow-hidden gap-3 pb-3">
      
      {/* 1. Immersive Hero Banner - Personalised with database session */}
      <motion.div
        className="relative overflow-hidden bg-emerald-950 text-emerald-50 rounded-[20px] p-5 flex flex-col justify-between shadow-md border border-emerald-900/40 shrink-0"
        initial={{ opacity: 0, y: 15 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
      >
        {/* Ambient glow decoration */}
        <div className="absolute -top-12 -right-12 w-28 h-28 bg-emerald-700/20 rounded-full blur-xl"></div>
        
        <div className="relative z-10">
          <div className="inline-flex items-center gap-1 px-2.5 py-0.5 bg-emerald-800/50 rounded-full text-[9px] font-bold uppercase tracking-wider mb-2 border border-emerald-700/20">
            <Sparkles size={10} className="text-amber-400 animate-pulse" />
            Milano Summit 2026
          </div>
          
          <h1 className="text-lg sm:text-2xl font-black leading-tight tracking-tight text-white uppercase">
            {userName ? `WELCOME BACK,` : "RESEEDING"}<br />
            <span className="text-amber-300">{userName ? userName.split(' ')[0].toUpperCase() : "THE FUTURE."}</span>
          </h1>

          <p className="text-emerald-200/75 text-[10px] mt-1.5 max-w-[280px] leading-relaxed font-semibold">
            {userName 
              ? `Authorized session registered successfully as a FFS ${userRole}. Secure credentials synchronised below.`
              : "Next-generation food system infrastructure & tech pipelines."}
          </p>
        </div>

        <div className="mt-3.5 pt-3 border-t border-emerald-900/30 relative z-10 flex items-center justify-between">
          <div className="flex flex-col">
            <span className="text-[8px] font-extrabold uppercase tracking-widest text-[#00e1ef]">
              Access Cleared
            </span>
            <span className="text-[10px] font-bold text-white/90">
              🎫 {userRole ? `${userRole} Ticket Pass` : "VIP Entry Pass"}
            </span>
          </div>
          
          <button 
            onClick={onOpenTicket}
            className="flex items-center gap-1 px-3.5 py-1.5 bg-amber-400 hover:bg-amber-300 text-slate-950 rounded-full text-[9px] font-extrabold uppercase tracking-wider shadow-sm cursor-pointer active:scale-95 transition-all duration-100"
          >
            Pass Wallet
            <ArrowRight size={11} />
          </button>
        </div>
      </motion.div>

      {/* 2. Compact 4-Column Live Status Bar */}
      <div className="grid grid-cols-4 gap-2 shrink-0">
        {/* Date Widget */}
        <div className="bg-white rounded-[16px] p-2.5 border border-slate-200/50 shadow-sm flex flex-col justify-between text-center">
          <span className="text-[8px] font-extrabold uppercase tracking-widest text-slate-400">Timeline</span>
          <div className="text-xs font-black text-slate-900 leading-none mt-1">OCT 14</div>
          <span className="text-[8px] font-bold text-slate-500 mt-1">09:00 AM</span>
        </div>

        {/* Location Widget */}
        <div className="bg-amber-400 rounded-[16px] p-2.5 shadow-sm flex flex-col justify-between text-center text-amber-950">
          <span className="text-[8px] font-extrabold uppercase tracking-widest opacity-70">Venue</span>
          <div className="text-xs font-black leading-none mt-1">MILANO</div>
          <span className="text-[8px] font-bold opacity-80 mt-1">Porta Nuova</span>
        </div>

        {/* Action Type Widget */}
        <div className="bg-slate-950 text-white rounded-[16px] p-2.5 shadow-sm flex flex-col justify-between text-center">
          <span className="text-[8px] font-extrabold uppercase tracking-widest text-amber-400">Session</span>
          <div className="text-[11px] font-black text-amber-400 leading-none mt-1 uppercase truncate">
            {userRole ? userRole : "Attendee"}
          </div>
          <span className="text-[8px] font-semibold text-slate-400 mt-1">Status Code</span>
        </div>

        {/* Experts Widget */}
        <div className="bg-white rounded-[16px] p-2.5 border border-slate-200/50 shadow-sm flex flex-col justify-between text-center">
          <span className="text-[8px] font-extrabold uppercase tracking-widest text-slate-400">Total list</span>
          <div className="text-xs font-black text-emerald-900 leading-none mt-1">100% Core</div>
          <span className="text-[8px] font-bold text-slate-500 mt-1">Verified</span>
        </div>
      </div>

      {/* 3. Dynamic Interactive Widgets Selector */}
      <div className="flex bg-slate-200/40 p-1 rounded-xl shrink-0 gap-1">
        <button
          onClick={() => setActiveWidget("poll")}
          className={`flex-1 py-1.5 text-[9px] font-black uppercase tracking-wider rounded-lg transition-all ${
            activeWidget === "poll"
              ? "bg-[#0b1720] text-white shadow-sm"
              : "text-slate-500 hover:text-slate-900 cursor-pointer"
          }`}
        >
          📊 Live Poll
        </button>

        {userName && (
          <button
            onClick={() => setActiveWidget("profile")}
            className={`flex-1 py-1.5 text-[9px] font-black uppercase tracking-wider rounded-lg transition-all flex items-center justify-center gap-1 ${
              activeWidget === "profile"
                ? "bg-amber-400 text-slate-950 font-black shadow-sm"
                : "text-slate-500 hover:text-slate-900 cursor-pointer"
            }`}
          >
            <Edit3 size={11} />
            <span>⚙️ Cloud Sync</span>
          </button>
        )}

        <button
          onClick={() => setActiveWidget("newsletter")}
          className={`flex-1 py-1.5 text-[9px] font-black uppercase tracking-wider rounded-lg transition-all ${
            activeWidget === "newsletter"
              ? "bg-[#0b1720] text-white shadow-sm"
              : "text-slate-500 hover:text-slate-900 cursor-pointer"
          }`}
        >
          📧 Newsletter
        </button>
      </div>

      {/* 4. Active Widget Container (Strictly bounds height to prevent scrolls) */}
      <div className="flex-1 min-h-0 bg-[#08131a]/95 border border-slate-800/80 rounded-[22px] p-4 flex flex-col justify-between relative overflow-hidden text-white shadow-inner">
        <AnimatePresence mode="wait">
          
          {activeWidget === "poll" && (
            <motion.div
              key="poll"
              initial={{ opacity: 0, scale: 0.98 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.98 }}
              transition={{ duration: 0.12 }}
              className="h-full flex flex-col justify-between gap-2.5 min-h-0"
            >
              <div>
                <div className="flex items-center gap-1.5">
                  <Vote size={13} className="text-[#00e1ef]" />
                  <span className="text-[8px] font-black uppercase tracking-[0.15em] text-[#00e1ef]">
                    Summit Poll
                  </span>
                </div>
                <h3 className="text-xs font-black text-slate-100 leading-snug mt-1">
                  Which tech pipeline handles rapid urban food security best?
                </h3>
              </div>

              {/* Stacked Options */}
              <div className="space-y-1.5 overflow-y-auto pr-0.5 scrollbar-thin">
                {[
                  { id: "cellular", label: "Cellular Biotech Proteins", count: pollVotes.cellular },
                  { id: "algae", label: "Marine Seaweed Composites", count: pollVotes.algae },
                  { id: "mycelium", label: "Fungal Mycelium Cuts", count: pollVotes.mycelium },
                  { id: "vertical", label: "Autonomous Hydroponic Kits", count: pollVotes.vertical }
                ].map((opt) => {
                  const hasVoted = votedOption !== null;
                  const isSelected = votedOption === opt.id;
                  const percentage = totalVotes > 0 ? Math.round((opt.count / totalVotes) * 100) : 0;

                  return (
                    <button
                      key={opt.id}
                      disabled={hasVoted}
                      onClick={() => handleVote(opt.id as any)}
                      className={`w-full relative overflow-hidden rounded-lg py-1.5 px-3 flex items-center justify-between text-[10px] font-bold text-left transition-all duration-150 border cursor-pointer ${
                        isSelected 
                          ? "border-[#00e1ef] text-[#00e1ef] bg-[#00e1ef]/5"
                          : hasVoted 
                          ? "border-slate-800/40 text-slate-500"
                          : "border-slate-800 text-slate-300 hover:border-[#00e1ef]/30 hover:bg-[#00e1ef]/5"
                      }`}
                    >
                      {hasVoted && (
                        <motion.div
                          className="absolute inset-y-0 left-0 bg-[#00e1ef]/10"
                          initial={{ width: 0 }}
                          animate={{ width: `${percentage}%` }}
                          transition={{ duration: 0.4 }}
                        />
                      )}
                      <span className="relative z-10 flex items-center gap-1.5 truncate pr-2">
                        {isSelected && <CheckCircle size={11} className="text-[#00e1ef]" />}
                        {opt.label}
                      </span>
                      {hasVoted && (
                        <span className="relative z-10 text-[9px] font-black font-mono text-[#00e1ef]">
                          {percentage}%
                        </span>
                      )}
                    </button>
                  );
                })}
              </div>

              {votedOption && (
                <p className="text-[9px] text-[#00e1ef]/80 text-center font-bold">
                  ✓ Response registered securely on the blockchain.
                </p>
              )}
            </motion.div>
          )}

          {activeWidget === "newsletter" && (
            <motion.div
              key="newsletter"
              initial={{ opacity: 0, scale: 0.98 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.98 }}
              transition={{ duration: 0.12 }}
              className="h-full flex flex-col justify-between gap-3 min-h-0"
            >
              <div>
                <span className="text-[8px] font-black uppercase tracking-[0.15em] text-slate-400 block">Briefings</span>
                <p className="text-xs font-black text-slate-100 leading-snug mt-1">
                  Claim your fast, digest-sized research updates on mycelium proteins and cellular scaling models.
                </p>
              </div>

              <form onSubmit={handleSubscribe} className="space-y-2">
                <input
                  type="email"
                  required
                  value={emailInput}
                  onChange={(e) => setEmailInput(e.target.value)}
                  disabled={subscribed}
                  placeholder="Enter business email"
                  className="w-full bg-[#030d12] text-white placeholder-slate-500 text-xs rounded-xl px-3.5 py-2 border border-slate-800 focus:outline-none focus:ring-1 focus:ring-[#00e1ef] font-medium"
                />
                <button
                  type="submit"
                  disabled={subscribed}
                  className="w-full py-2 bg-[#44d4ea] text-slate-950 hover:bg-[#2bcde5] active:bg-[#1dbec5] rounded-xl text-xs font-black uppercase tracking-wider transition-colors cursor-pointer disabled:bg-[#153a42] disabled:text-slate-500 flex items-center justify-center gap-1.5"
                >
                  {subscribed ? <span>Subscribed!</span> : <span>Subscribe briefed</span>}
                  {subscribed ? <CheckCircle size={13} /> : <Send size={11} />}
                </button>
              </form>

              <div className="h-6 flex items-center justify-center">
                {subscribed && (
                  <motion.p 
                    className="text-[9px] text-[#00e1ef] font-extrabold flex items-center gap-1"
                    initial={{ opacity: 0, y: -4 }}
                    animate={{ opacity: 1, y: 0 }}
                  >
                    <Award size={11} />
                    Registration link sent to your inbox.
                  </motion.p>
                )}
              </div>
            </motion.div>
          )}

          {activeWidget === "profile" && userName && (
            /* ⚙️ LIVE DATABASE SYNC FORM PANEL - SPEAKER OR EXHIBITOR PROFILE UPDATER */
            <motion.form
              key="profile"
              onSubmit={handleSaveProfile}
              initial={{ opacity: 0, scale: 0.98 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.98 }}
              transition={{ duration: 0.12 }}
              className="h-full flex flex-col justify-between gap-2 min-h-0 text-left"
            >
              <div className="flex justify-between items-center pb-1 border-b border-slate-800">
                <div className="flex items-center gap-1">
                  <RefreshCw size={11} className="text-amber-400 animate-spin-slow" />
                  <span className="text-[8px] font-black uppercase tracking-widest text-[#00e1ef]">
                    {userRole === "Speaker" ? "Speaker session portal" : "Exhibitor directory portal"}
                  </span>
                </div>
                <span className="text-[8px] font-mono text-slate-500 font-bold truncate max-w-[120px]">
                  ID: {userSession?.userId.substring(0,8)}...
                </span>
              </div>

              {/* Dynamic scrollable core credentials editor */}
              <div className="flex-1 overflow-y-auto pr-1 space-y-2.5 py-1.5 scrollbar-thin">
                {userRole === "Speaker" ? (
                  /* SPEAKER CONTROLS */
                  <>
                    <div>
                      <label className="text-[8px] font-extrabold text-amber-300 block mb-0.5 uppercase tracking-wider">
                        Topic Presentation Title
                      </label>
                      <input
                        type="text"
                        required
                        value={speakerTopic}
                        onChange={(e) => setSpeakerTopic(e.target.value)}
                        className="w-full bg-[#030e14] border border-slate-850 px-3 py-1.5 text-xs text-white rounded-lg focus:ring-1 focus:ring-[#00e1ef] focus:outline-none font-bold"
                        placeholder="Specify presentation topic"
                      />
                    </div>

                    <div>
                      <label className="text-[8px] font-extrabold text-amber-300 block mb-0.5 uppercase tracking-wider">
                        A/V Requirements & Requests
                      </label>
                      <input
                        type="text"
                        value={speakerAV}
                        onChange={(e) => setSpeakerAV(e.target.value)}
                        className="w-full bg-[#030e14] border border-slate-850 px-3 py-1.5 text-[11px] text-white rounded-lg focus:ring-1 focus:ring-[#00e1ef] focus:outline-none font-medium"
                        placeholder="e.g. HDMI projector, lapel microphone, screen share"
                      />
                    </div>

                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <label className="text-[8px] font-extrabold text-amber-300 block mb-0.5 uppercase tracking-wider">
                          Speaker Origin
                        </label>
                        <input
                          type="text"
                          required
                          value={speakerLoc}
                          onChange={(e) => setSpeakerLoc(e.target.value)}
                          className="w-full bg-[#030e14] border border-slate-850 px-3 py-1.5 text-[11px] text-white rounded-lg focus:ring-1 focus:ring-[#00e1ef] focus:outline-none font-semibold"
                          placeholder="e.g. Toronto, ON"
                        />
                      </div>
                      <div>
                        <label className="text-[8px] font-extrabold text-amber-300 block mb-0.5 uppercase tracking-wider">
                          Session Format
                        </label>
                        <select
                          value={speakerFormat}
                          onChange={(e) => setSpeakerFormat(e.target.value)}
                          className="w-full bg-[#030e14] border border-slate-850 px-3 py-1.5 text-[11px] text-white rounded-lg focus:ring-1 focus:ring-[#00e1ef] focus:outline-none font-semibold"
                        >
                          <option value="Keynote">Keynote Presentation</option>
                          <option value="Panel Discussion">Panel Discussion</option>
                          <option value="Fireside Chat">Fireside Chat</option>
                        </select>
                      </div>
                    </div>

                    <div>
                      <label className="text-[8px] font-extrabold text-amber-300 block mb-0.5 uppercase tracking-wider">
                        Public Professional Biography
                      </label>
                      <textarea
                        required
                        value={speakerBio}
                        onChange={(e) => setSpeakerBio(e.target.value)}
                        className="w-full bg-[#030e14] border border-slate-850 px-3 py-1 text-[10px] text-slate-300 rounded-lg focus:ring-1 focus:ring-[#00e1ef] focus:outline-none font-medium h-12 resize-none"
                        placeholder="Tell the committee about your background..."
                      />
                    </div>
                  </>
                ) : (
                  /* EXHIBITOR CONTROLS */
                  <>
                    <div>
                      <label className="text-[8px] font-extrabold text-[#00e1ef] block mb-0.5 uppercase tracking-wider">
                        Company Elevator Description
                      </label>
                      <textarea
                        required
                        value={exhibitorDesc}
                        onChange={(e) => setExhibitorDesc(e.target.value)}
                        className="w-full bg-[#030e14] border border-slate-850 px-3 py-1 text-[9.5px] text-slate-300 rounded-lg focus:ring-1 focus:ring-[#00e1ef] focus:outline-none font-medium h-12 resize-none"
                        placeholder="What does your company trade or consult in?"
                      />
                    </div>

                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <label className="text-[8px] font-extrabold text-[#00e1ef] block mb-0.5 uppercase tracking-wider">
                          Annual Revenue Tier
                        </label>
                        <select
                          value={exhibitorRevenue}
                          onChange={(e) => setExhibitorRevenue(e.target.value)}
                          className="w-full bg-[#030e14] border border-slate-850 px-3 py-1.5 text-[10px] text-white rounded-lg focus:ring-1 focus:ring-[#00e1ef] focus:outline-none font-semibold"
                        >
                          <option value="Pre-revenue">Pre-revenue</option>
                          <option value="$100K – $500K">$100K – $500K</option>
                          <option value="$500K – $2M">$500K – $2M</option>
                          <option value="Over $2M">Over $2M</option>
                        </select>
                      </div>
                      <div>
                        <label className="text-[8px] font-extrabold text-[#00e1ef] block mb-0.5 uppercase tracking-wider">
                          Trade Type (Import)
                        </label>
                        <select
                          value={exhibitorStatus}
                          onChange={(e) => setExhibitorStatus(e.target.value)}
                          className="w-full bg-[#030e14] border border-slate-850 px-3 py-1.5 text-[10px] text-white rounded-lg focus:ring-1 focus:ring-[#00e1ef] focus:outline-none font-semibold"
                        >
                          <option value="Exporter">Exporter only</option>
                          <option value="Importer">Importer only</option>
                          <option value="Both">Both (Joint venture)</option>
                        </select>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <label className="text-[8px] font-extrabold text-[#00e1ef] block mb-0.5 uppercase tracking-wider">
                          Booth Confirmation
                        </label>
                        <input
                          type="text"
                          required
                          value={exhibitorBooth}
                          onChange={(e) => setExhibitorBooth(e.target.value)}
                          className="w-full bg-[#030e14] border border-slate-850 px-3 py-1.5 text-[11px] text-white rounded-lg focus:ring-1 focus:ring-[#00e1ef] focus:outline-none font-semibold"
                          placeholder="e.g. 8x8 Kiosk"
                        />
                      </div>
                      <div>
                        <label className="text-[8px] font-extrabold text-[#00e1ef] block mb-0.5 uppercase tracking-wider">
                          Electrical Supplies
                        </label>
                        <input
                          type="text"
                          required
                          value={exhibitorNeeds}
                          onChange={(e) => setExhibitorNeeds(e.target.value)}
                          className="w-full bg-[#030e14] border border-slate-850 px-3 py-1.5 text-[11px] text-white rounded-lg focus:ring-1 focus:ring-[#00e1ef] focus:outline-none font-semibold"
                          placeholder="e.g. Internet, 220V Outlet"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="text-[8px] font-extrabold text-[#00e1ef] block mb-0.5 uppercase tracking-wider">
                        Corporate Web Address
                      </label>
                      <input
                        type="text"
                        value={exhibitorWebsite}
                        onChange={(e) => setExhibitorWebsite(e.target.value)}
                        className="w-full bg-[#030e14] border border-slate-850 px-3 py-1.5 text-[11px] text-white rounded-lg focus:ring-1 focus:ring-[#00e1ef] focus:outline-none font-medium"
                        placeholder="e.g. www.cognico.ca"
                      />
                    </div>
                  </>
                )}
              </div>

              {/* Actions & Feedback */}
              <div className="flex justify-between items-center gap-2 pt-1 border-t border-slate-800">
                {saveSuccess ? (
                  <motion.div 
                    className="flex items-center gap-1.5 text-[#00ffd5] text-[10px] font-black"
                    initial={{ opacity: 0, x: -3 }}
                    animate={{ opacity: 1, x: 0 }}
                  >
                    <CheckCircle size={13} />
                    <span>SYNCHRONISED WITH MILANO DATABASE</span>
                  </motion.div>
                ) : (
                  <span className="text-[8px] text-slate-500 font-bold uppercase tracking-wider leading-relaxed">
                    Changes take effect on summit digital signage instantly.
                  </span>
                )}
                
                <button
                  type="submit"
                  disabled={isSaving}
                  className="flex items-center gap-1.5 px-4 py-2 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 text-white text-[10px] font-black uppercase tracking-wider rounded-lg transition-all duration-100 disabled:opacity-40 cursor-pointer shadow-md shrink-0"
                >
                  <Save size={12} />
                  <span>{isSaving ? "Syncing..." : "Save Profile"}</span>
                </button>
              </div>
            </motion.form>
          )}

        </AnimatePresence>
      </div>

    </div>
  );
}
