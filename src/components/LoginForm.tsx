import { useState, FormEvent } from "react";
import { motion, AnimatePresence } from "motion/react";
import { 
  Globe, 
  Briefcase, 
  Apple, 
  Mail, 
  ArrowRight, 
  ChevronRight, 
  AlertCircle, 
  ShieldCheck, 
  Lock, 
  ChevronLeft, 
  HelpCircle, 
  Check, 
  Copy,
  Users,
  Award
} from "lucide-react";

export interface UserSession {
  userId: string;
  email: string;
  displayName: string;
  role: string;
  profile: any;
}

interface LoginFormProps {
  onLogin: (session: UserSession) => void;
}

export default function LoginForm({ onLogin }: LoginFormProps) {
  const [email, setEmail] = useState("");
  const [uuid, setUuid] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  
  // Controls authentication steps: "method" -> choose login option; "uuid" -> input master UUID credentials
  const [step, setStep] = useState<"method" | "uuid">("method");
  const [loginType, setLoginType] = useState<"Email" | "Google" | "LinkedIn" | "Apple">("Email");
  
  // Help lookup modal toggle
  const [showLookup, setShowLookup] = useState(false);
  const [copiedId, setCopiedId] = useState<string | null>(null);

  // Seeded UUID Lookup for helpful demonstration purposes (representing the user's uploaded CSVs)
  const sampleCredentials = {
    exhibitors: [
      { id: "feb8a00c-839e-4412-80c8-2e76765a1014", name: "CogniCo (Sponsor)", code: "feb8a00c..." },
      { id: "f4f09cb2-c7c0-49c0-b776-225ee4155cdb", name: "Food Forward", code: "f4f09cb2..." },
      { id: "9da28dbe-3b1e-4e60-834e-69e13f2ecf9c", name: "Cognico_test", code: "9da28dbe..." }
    ],
    speakers: [
      { id: "884384d3-2093-41e2-8b7f-8299e0ba4615", name: "Alexander Kappes", title: "Livestock Technology Sponsor" },
      { id: "ad67408c-15ad-4164-b4f0-45a68dbba69d", name: "Ashley Nicholls", title: "REACH Agriculture Strats" },
      { id: "33f4effc-14e6-4774-b2ea-0fc72d07c670", name: "Julie Francoeur", title: "CEO Fairtrade Canada" },
      { id: "89302393-6920-41df-a3b6-61ad346830c2", name: "Meifan Shi", title: "Waterpoint Lane VC" }
    ]
  };

  const handleCopy = (id: string) => {
    navigator.clipboard.writeText(id);
    setCopiedId(id);
    setUuid(id); // Auto-populate inside UUID field
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleEmailSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!email) {
      setError("Please enter your business email address");
      return;
    }
    if (!/\S+@\S+\.\S+/.test(email)) {
      setError("Please enter a valid business email address");
      return;
    }
    setError("");
    setLoginType("Email");
    // Transition to UUID verification
    setStep("uuid");
  };

  const handleSocialSelect = (platform: "Google" | "LinkedIn" | "Apple") => {
    setError("");
    setLoginType(platform);
    // Transition to UUID verification
    setStep("uuid");
  };

  const handleAuthenticateUuid = async (e: FormEvent) => {
    e.preventDefault();
    if (!uuid.trim()) {
      setError("Please enter your unique account identification code.");
      return;
    }

    setError("");
    setIsLoading(true);

    try {
      const response = await fetch("/api/auth", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          uuid: uuid.trim(),
          loginType,
          email: email.trim() || undefined
        })
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.error || "Authentication security handshake rejected.");
      }

      // If successfully authenticated from the database, transition to App
      onLogin(result);
    } catch (err: any) {
      setError(err.message || "UUID credential was not found in the summit secure database.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="h-screen w-screen flex items-center justify-center p-6 bg-[#08101d] text-white font-sans selection:bg-[#00e1ef] selection:text-slate-950 overflow-hidden relative">
      
      {/* Background visual geometric grid overlay */}
      <div className="absolute inset-0 bg-[radial-gradient(#0c2537_1px,transparent_1px)] [background-size:16px_16px] opacity-30 pointer-events-none"></div>

      <div className="w-full max-w-[390px] flex flex-col justify-center h-full max-h-[95vh] sm:max-h-[820px] relative z-10">
        
        {/* Top Logo and Header */}
        <div className="text-center mb-4 shrink-0">
          {/* White Squircle Brand Container */}
          <motion.div 
            className="w-14 h-14 bg-white rounded-[18px] flex items-center justify-center mx-auto mb-3 shadow-xl border border-slate-200/20"
            initial={{ scale: 0.8, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ type: "spring", stiffness: 200, damping: 15 }}
          >
            <img
              src="/src/assets/images/FF Favicon.png"
              alt="Food Forward Logo"
              className="w-10 h-10 object-contain"
              referrerPolicy="no-referrer"
            />
          </motion.div>

          <h2 className="text-[20px] font-black tracking-tight text-white leading-none uppercase">
            Food Forward <span className="text-[#00e1ef]">Summit</span>
          </h2>
          <p className="text-slate-400 text-[10px] font-semibold leading-relaxed tracking-wide mt-2 px-4">
            Security Clearance & Matchmaking Gateway for Registered Exhibitors, Speakers & Sponsors
          </p>
        </div>

        {/* Dynamic Card Container Switch (Methods vs UUID validation) */}
        <div className="relative">
          <AnimatePresence mode="wait">
            {step === "method" ? (
              /* PANEL A: LOGIN TYPE SELECTION */
              <motion.div
                key="method-panel"
                className="bg-[#081d26] rounded-[22px] p-5 border border-[#0d2a37] shadow-2xl w-full"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.2 }}
              >
                <div className="flex justify-between items-center mb-3">
                  <h3 className="text-[10px] font-extrabold uppercase tracking-widest text-[#00e1ef]">
                    Step 1: SELECT ACCOUNT TYPE
                  </h3>
                  <button 
                    onClick={() => setShowLookup(!showLookup)}
                    className="flex items-center gap-1 text-[9px] font-extrabold px-2 py-1 rounded-md bg-[#0c2e3d] text-amber-300 hover:bg-[#0f3c50] transition cursor-pointer"
                  >
                    <HelpCircle size={10} />
                    Lookup Codes
                  </button>
                </div>

                <form onSubmit={handleEmailSubmit} className="space-y-3">
                  {/* Business Email Field */}
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                      <Mail className="h-4.5 w-4.5 text-[#45d4ea] opacity-80" />
                    </div>
                    <input
                      type="email"
                      value={email}
                      onChange={(e) => {
                        setEmail(e.target.value);
                        if (error) setError("");
                      }}
                      placeholder="Enter Business Email Address"
                      className={`w-full bg-[#030e14] text-white placeholder-slate-500 text-xs rounded-[12px] pl-10 pr-4 py-3.5 border focus:outline-none focus:ring-1 focus:ring-[#00e1ef] transition font-bold ${
                        error ? "border-red-500" : "border-[#153444] focus:border-[#00e1ef]"
                      }`}
                    />
                  </div>

                  {error && (
                    <div className="flex items-center gap-1.5 text-red-400 text-[10px] mt-1 px-1 font-bold">
                      <AlertCircle size={12} />
                      <span>{error}</span>
                    </div>
                  )}

                  <button
                    type="submit"
                    className="w-full flex items-center justify-center gap-2 py-3.5 bg-[#44d4ea] hover:bg-[#2bcde5] text-[#030e14] font-extrabold rounded-full text-[11px] uppercase tracking-wider transition-all cursor-pointer shadow-md"
                  >
                    <span>Continue with Email</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </button>
                </form>

                {/* Secure Divider */}
                <div className="flex items-center gap-3 my-4">
                  <div className="flex-1 h-[1px] bg-[#112431]"></div>
                  <span className="text-[8px] font-black uppercase tracking-[0.2em] text-slate-500">
                    OR SECURELY CONNECT WITH
                  </span>
                  <div className="flex-1 h-[1px] bg-[#112431]"></div>
                </div>

                {/* Social Integration Options */}
                <div className="space-y-2">
                  <button
                    onClick={() => handleSocialSelect("Google")}
                    className="w-full hover:bg-[#0c2c3b]/30 active:bg-slate-800/40 bg-[#080f1d]/40 border border-slate-700/30 text-white rounded-full py-3 px-6 flex items-center justify-center font-bold tracking-wide text-[11px] transition duration-150 relative cursor-pointer"
                  >
                    <div className="absolute left-5 flex items-center justify-center bg-[#0d2a37] w-6 h-6 rounded-full">
                      <Globe className="w-3.5 h-3.5 text-[#00aaff]" />
                    </div>
                    <span>Continue with Google</span>
                  </button>

                  <button
                    onClick={() => handleSocialSelect("LinkedIn")}
                    className="w-full hover:bg-[#0c2c3b]/30 active:bg-slate-800/40 bg-[#080f1d]/40 border border-slate-700/30 text-white rounded-full py-3 px-6 flex items-center justify-center font-bold tracking-wide text-[11px] transition duration-150 relative cursor-pointer"
                  >
                    <div className="absolute left-5 flex items-center justify-center bg-[#0d2a37] w-6 h-6 rounded-full">
                      <Briefcase className="w-3.5 h-3.5 text-amber-500" />
                    </div>
                    <span>Continue with LinkedIn</span>
                  </button>

                  <button
                    onClick={() => handleSocialSelect("Apple")}
                    className="w-full hover:bg-[#0c2c3b]/30 active:bg-slate-800/40 bg-[#080f1d]/40 border border-slate-700/30 text-white rounded-full py-3 px-6 flex items-center justify-center font-bold tracking-wide text-[11px] transition duration-150 relative cursor-pointer"
                  >
                    <div className="absolute left-5 flex items-center justify-center bg-[#0d2a37] w-6 h-6 rounded-full">
                      <Apple className="w-3.5 h-3.5 text-slate-300 fill-current" />
                    </div>
                    <span>Continue with Apple</span>
                  </button>
                </div>
              </motion.div>
            ) : (
              /* PANEL B: UUID SUBMISSION CARD */
              <motion.div
                key="uuid-panel"
                className="bg-[#0c2432] rounded-[22px] p-5 border border-[#11374a] shadow-2xl w-full"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.2 }}
              >
                <div className="flex items-center gap-2 mb-3">
                  <button 
                    onClick={() => {
                      setStep("method");
                      setError("");
                    }}
                    className="p-1 hover:bg-[#061821] rounded-full text-slate-400 hover:text-white transition cursor-pointer"
                    title="Change login method"
                  >
                    <ChevronLeft size={16} />
                  </button>
                  <h3 className="text-[10px] font-extrabold uppercase tracking-widest text-[#00e1ef]">
                    Step 2: SECURE SIGNATURE LOCK
                  </h3>
                </div>

                <div className="p-3.5 bg-[#030e14] rounded-xl border border-slate-800/40 mb-3.5">
                  <div className="flex gap-2 items-center text-emerald-400 mb-1">
                    <ShieldCheck size={14} />
                    <span className="text-[9px] uppercase font-black tracking-widest">
                      {loginType} verification pending
                    </span>
                  </div>
                  <p className="text-[10px] text-slate-400 font-semibold leading-relaxed">
                    To authenticate your professional {loginType} profile, please input your Unique Registration UUID.
                  </p>
                </div>

                <form onSubmit={handleAuthenticateUuid} className="space-y-3.5">
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                      <Lock className="h-4 w-4 text-[#00e1ef]" />
                    </div>
                    <input
                      type="text"
                      required
                      value={uuid}
                      onChange={(e) => {
                        setUuid(e.target.value);
                        if (error) setError("");
                      }}
                      placeholder="e.g. 884384d3-2093-41e2-b77f-..."
                      className={`w-full bg-[#030e14] text-white placeholder-slate-500 text-xs rounded-[12px] pl-10 pr-4 py-3.5 border focus:outline-none focus:ring-1 focus:ring-[#00e1ef] font-mono tracking-wide ${
                        error ? "border-red-500" : "border-[#1c3f56] focus:border-[#00e1ef]"
                      }`}
                    />
                  </div>

                  {error && (
                    <div className="flex items-start gap-1.5 text-red-400 text-[10px] p-2 bg-red-950/20 rounded-lg border border-red-900/30">
                      <AlertCircle size={13} className="shrink-0 mt-0.5" />
                      <span>{error}</span>
                    </div>
                  )}

                  <div className="flex gap-2.5">
                    <button
                      type="button"
                      onClick={() => setShowLookup(true)}
                      className="px-4 bg-[#143649] hover:bg-[#1d4c65] text-slate-200 font-bold text-xs rounded-xl transition cursor-pointer"
                    >
                      Lookup Codes
                    </button>
                    <button
                      type="submit"
                      disabled={isLoading}
                      className="flex-1 py-3.5 bg-gradient-to-r from-[#00ffd5] to-[#00bfff] hover:from-[#33ffd5] hover:to-[#33bfff] text-[#030e14] font-black rounded-xl text-xs uppercase tracking-wider transition-all duration-100 cursor-pointer shadow-lg disabled:opacity-50"
                    >
                      {isLoading ? "Validating Signature..." : "Verify & Sign In"}
                    </button>
                  </div>
                </form>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Dynamic Expandable Database Credentials Lookup drawer - Highly usable */}
        <AnimatePresence>
          {showLookup && (
            <motion.div
              className="absolute inset-x-0 bottom-1 bg-[#061219] border border-slate-700/50 rounded-[24px] p-4 text-white shadow-2xl overflow-y-auto max-h-[85vh] z-30"
              initial={{ y: "100%", opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              exit={{ y: "100%", opacity: 0 }}
              transition={{ type: "spring", damping: 25, stiffness: 220 }}
            >
              <div className="flex justify-between items-center mb-3 border-b border-slate-800 pb-2">
                <div className="flex items-center gap-1.5">
                  <Users size={14} className="text-amber-400" />
                  <span className="text-[10px] font-black uppercase tracking-widest text-[#00e1ef]">
                    Secure Registry Lookup
                  </span>
                </div>
                <button
                  onClick={() => setShowLookup(false)}
                  className="px-2.5 py-1 text-[9px] bg-red-950/40 text-red-300 border border-red-900/30 hover:bg-red-900/40 rounded-full cursor-pointer transition font-bold"
                >
                  Dismiss
                </button>
              </div>

              <p className="text-[9.5px] text-slate-400 font-semibold leading-relaxed mb-4">
                Click any pre-registered ID below. It will automatically populate the credential block for instant testing.
              </p>

              {/* SECTION 1: SPEAKERS */}
              <div className="space-y-2 mb-4">
                <div className="flex items-center gap-1">
                  <Award size={10} className="text-amber-400" />
                  <h4 className="text-[9px] font-black tracking-wider uppercase text-slate-300">
                    Pre-registered Speakers (CSV Database)
                  </h4>
                </div>
                <div className="space-y-1.5 max-h-[160px] overflow-y-auto pr-1">
                  {sampleCredentials.speakers.map((s) => (
                    <div 
                      key={s.id}
                      onClick={() => handleCopy(s.id)}
                      className="p-2 border border-slate-800 hover:border-[#00e1ef]/40 hover:bg-[#0a2330] rounded-xl flex justify-between items-center bg-[#040c11] cursor-pointer transition group"
                    >
                      <div className="truncate pr-2">
                        <span className="text-[10px] font-bold text-white block group-hover:text-[#00e1ef]">
                          {s.name}
                        </span>
                        <span className="text-[8.5px] text-slate-500 block truncate group-hover:text-slate-400">
                          {s.title}
                        </span>
                      </div>
                      <button className="p-1 px-1.5 text-[8px] font-extrabold bg-[#0d2a37] text-white/80 rounded-md group-hover:bg-[#00e1ef] group-hover:text-[#030e14] transition shrink-0 flex items-center gap-1">
                        {copiedId === s.id ? <Check size={8} /> : <Copy size={8} />}
                        <span>Select</span>
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* SECTION 2: EXHIBITORS */}
              <div className="space-y-2">
                <div className="flex items-center gap-1">
                  <Globe size={10} className="text-[#00e1ef]" />
                  <h4 className="text-[9px] font-black tracking-wider uppercase text-slate-300">
                    Pre-registered Exhibitors (CSV Database)
                  </h4>
                </div>
                <div className="space-y-1.5">
                  {sampleCredentials.exhibitors.map((e) => (
                    <div 
                      key={e.id}
                      onClick={() => handleCopy(e.id)}
                      className="p-2 border border-slate-800 hover:border-[#00e1ef]/40 hover:bg-[#0a2330] rounded-xl flex justify-between items-center bg-[#040c11] cursor-pointer transition group"
                    >
                      <div className="truncate pr-2">
                        <span className="text-[10px] font-bold text-white block group-hover:text-[#00e1ef]">
                          {e.name}
                        </span>
                        <span className="text-[8.5px] font-mono font-bold text-slate-600 block group-hover:text-slate-400">
                          Universal UUID: {e.code}
                        </span>
                      </div>
                      <button className="p-1 px-1.5 text-[8px] font-extrabold bg-[#0d2a37] text-white/80 rounded-md group-hover:bg-[#00e1ef] group-hover:text-[#030e14] transition shrink-0 flex items-center gap-1">
                        {copiedId === e.id ? <Check size={8} /> : <Copy size={8} />}
                        <span>Select</span>
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-800 text-center">
                <span className="text-[8px] font-black text-slate-500 uppercase tracking-widest block">
                  Click 'Dismiss' to return to gateway controls
                </span>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Footer/Trust Marker */}
        <div className="text-center mt-4 shrink-0 flex items-center justify-center gap-1.5 opacity-40">
          <ShieldCheck className="w-3.5 h-3.5 text-[#00e1ef]" />
          <span className="text-[8.5px] tracking-[0.2em] text-[#00e1ef] uppercase font-black">
            B2B Security Handshake Ready
          </span>
        </div>
      </div>
    </div>
  );
}
