import { useState, FormEvent } from "react";
import { motion, AnimatePresence } from "motion/react";
import { X, Award, CheckCircle, Smartphone, MapPin, Calendar, Sparkles } from "lucide-react";

interface TicketPassModalProps {
  isOpen: boolean;
  onClose: () => void;
  isRegistered: boolean;
  onRegister: () => void;
  userEmail: string;
  userName?: string;
  userRole?: string;
}

export default function TicketPassModal({ 
  isOpen, 
  onClose, 
  isRegistered, 
  onRegister, 
  userEmail,
  userName,
  userRole
}: TicketPassModalProps) {
  const [fullName, setFullName] = useState(userName || "Alexander Sterling");
  const [tier, setTier] = useState<string>(
    userRole === "Speaker" ? "VIP Gold Speaker" :
    userRole === "Sponsor" ? "Industry Sponsor" :
    userRole === "Exhibitor" ? "Exhibitor Pass" : "Attendee"
  );
  const [success, setSuccess] = useState(false);

  const handleRegisterFormSubmit = (e: FormEvent) => {
    e.preventDefault();
    onRegister();
    setSuccess(true);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm select-none">
      <motion.div
        className="w-full max-w-[370px] bg-[#0c1822] text-white rounded-[28px] overflow-hidden shadow-2xl border border-slate-800"
        initial={{ scale: 0.9, opacity: 0, y: 30 }}
        animate={{ scale: 1, opacity: 1, y: 0 }}
        exit={{ scale: 0.9, opacity: 0, y: 30 }}
        transition={{ type: "spring", damping: 20, stiffness: 300 }}
      >
        {/* Header Block with custom Dismiss button */}
        <div className="p-4 bg-[#0a141b] border-b border-slate-800/80 flex justify-between items-center">
          <span className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-[#00e1ef]">
            {isRegistered ? "WALLET PASS ACTIVE" : "CLAIM ENTRY PASS"}
          </span>
          <button 
            onClick={onClose} 
            className="p-1 px-2 hover:bg-slate-800 rounded-full transition text-slate-400 cursor-pointer"
          >
            <X size={15} />
          </button>
        </div>

        {/* Dynamic Pass Render based on registration state */}
        <div className="p-6">
          {!isRegistered ? (
            /* REGISTRATION FORM STATE */
            <form onSubmit={handleRegisterFormSubmit} className="space-y-4">
              <div className="text-center">
                <div className="w-12 h-12 bg-emerald-950 rounded-full flex items-center justify-center mx-auto mb-2 border border-emerald-800">
                  <Sparkles size={20} className="text-[#00e1ef]" />
                </div>
                <h4 className="text-sm font-black text-white">Generate Mobile Entry Pass</h4>
                <p className="text-[11px] text-slate-400 mt-1">Ready your access badge for on-site scanners in Milano.</p>
              </div>

              <div className="space-y-3 pt-2">
                <div>
                  <label className="text-[9px] font-extrabold uppercase tracking-widest text-[#00e1ef] block mb-1">Full Name</label>
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="Enter full name"
                    className="w-full bg-[#030e14] text-white text-xs rounded-xl px-4 py-3 border border-[#162e3d] focus:outline-none focus:ring-1 focus:ring-[#00e1ef] font-bold"
                  />
                </div>

                <div>
                  <label className="text-[9px] font-extrabold uppercase tracking-widest text-[#00e1ef] block mb-1">Pass Access Tier</label>
                  <select 
                    value={tier}
                    onChange={(e) => setTier(e.target.value as any)}
                    className="w-full bg-[#030e14] text-white text-xs rounded-xl px-4 py-3 border border-[#162e3d] focus:outline-none focus:ring-1 focus:ring-[#00e1ef] font-bold"
                  >
                    <option value="Attendee">Attendee Access - $299</option>
                    <option value="VIP Gold Speaker">VIP Gold Speaker - Guest</option>
                    <option value="Industry Sponsor">Industry Sponsor - Complimentary</option>
                  </select>
                </div>

                <div>
                  <label className="text-[9px] font-extrabold uppercase tracking-widest text-[#00e1ef] block mb-1">Account Email</label>
                  <input
                    type="email"
                    disabled
                    value={userEmail}
                    className="w-full bg-[#0c1822]/40 text-slate-400 text-xs rounded-xl px-4 py-3 border border-slate-800 font-medium"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="w-full mt-4 py-3.5 bg-[#44d4ea] hover:bg-[#2bcde5] text-slate-950 font-black rounded-full text-xs uppercase tracking-wider cursor-pointer active:scale-95 transition"
              >
                Assemble Wallet Pass
              </button>
            </form>
          ) : (
            /* ACTIVE PASS RENDER (Apple Wallet concept) */
            <div className="space-y-5">
              {/* Wallet Ticket Body */}
              <motion.div 
                className="bg-emerald-900 rounded-[20px] p-5 shadow-xl border border-emerald-800 relative overflow-hidden"
                initial={{ rotate: -1, y: 10 }}
                animate={{ rotate: 0, y: 0 }}
              >
                {/* Visual Holes on left/right for pass ticket effect */}
                <div className="absolute -left-3 top-1/2 -translate-y-1/2 w-6 h-6 bg-[#0c1822] rounded-full"></div>
                <div className="absolute -right-3 top-1/2 -translate-y-1/2 w-6 h-6 bg-[#0c1822] rounded-full"></div>

                <div className="flex justify-between items-start">
                  <div>
                    <span className="text-[8px] font-extrabold uppercase tracking-wider text-emerald-300">Milano 2026</span>
                    <h5 className="text-sm font-black text-white leading-none mt-0.5">FOOD FORWARD SEMMIT</h5>
                  </div>
                  <Award size={18} className="text-amber-400" />
                </div>

                {/* Main Pass visual divider line */}
                <div className="border-t border-dashed border-emerald-850/40 my-4 pt-4 flex justify-between gap-2">
                  <div>
                    <span className="text-[8px] tracking-widest text-emerald-300 block">HOLDER</span>
                    <span className="text-[11px] font-extrabold text-white">{fullName}</span>
                  </div>
                  <div className="text-right">
                    <span className="text-[8px] tracking-widest text-emerald-300 block">ACCESS</span>
                    <span className="text-[11px] font-extrabold text-amber-300">{tier}</span>
                  </div>
                </div>

                <div className="flex justify-between gap-2 mt-2">
                  <div className="flex items-center gap-1 text-[10px] text-emerald-200">
                    <MapPin size={10} />
                    <span>Milano, Italy</span>
                  </div>
                  <div className="flex items-center gap-1 text-[10px] text-emerald-200">
                    <Calendar size={10} />
                    <span>OCT 14</span>
                  </div>
                </div>

                {/* Simulated NFC / Scanner Barcode bottom block */}
                <div className="mt-6 pt-4 border-t border-emerald-800/60 flex flex-col items-center">
                  {/* Pseudo Barcode Lines */}
                  <div className="bg-white p-2.5 rounded-lg flex gap-1.5 items-center justify-center w-full max-w-[200px]">
                    <div className="h-9 w-1 bg-slate-950 rounded-sm"></div>
                    <div className="h-9 w-2 bg-slate-950 rounded-sm"></div>
                    <div className="h-9 w-0.5 bg-slate-950 rounded-sm"></div>
                    <div className="h-9 w-1.5 bg-slate-950 rounded-sm"></div>
                    <div className="h-9 w-2.5 bg-slate-950 rounded-sm"></div>
                    <div className="h-9 w-0.5 bg-slate-950 rounded-sm"></div>
                    <div className="h-9 w-1.5 bg-slate-950 rounded-sm"></div>
                    <div className="h-9 w-1 bg-slate-950 rounded-sm"></div>
                    <div className="h-9 w-2 bg-slate-950 rounded-sm"></div>
                    <div className="h-9 w-0.5 bg-slate-950 rounded-sm"></div>
                    <div className="h-9 w-1 bg-slate-950 rounded-sm"></div>
                  </div>
                  <span className="text-[8px] font-mono font-bold text-emerald-200/60 mt-2 tracking-[0.25em]">FFS-928A-MILANO</span>
                </div>
              </motion.div>

              <div className="text-center text-[10px] text-slate-400 font-semibold">
                ⭐️ Keep this pass open on entry gates or tap with NFC for check-in.
              </div>

              <button
                onClick={() => {
                  alert("Simulated: Added directly to your device local Apple/Google Wallet cache!");
                  onClose();
                }}
                className="w-full flex items-center justify-center gap-2 py-3 bg-[#0a141b] border border-slate-700/60 hover:bg-slate-800 text-slate-200 font-extrabold rounded-full text-xs uppercase tracking-wide cursor-pointer active:scale-95 transition"
              >
                <Smartphone size={13} className="text-[#00e1ef]" />
                <span>Add to Phone Wallet</span>
              </button>
            </div>
          )}
        </div>
      </motion.div>
    </div>
  );
}
