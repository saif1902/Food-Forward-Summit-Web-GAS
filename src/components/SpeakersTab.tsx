import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { Search, Linkedin, MapPin, Notebook, Award, ExternalLink, RefreshCw } from "lucide-react";
import { speakersList } from "../data";
import { SpeakerEntity } from "../types";

export default function SpeakersTab() {
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedSpeakerId, setSelectedSpeakerId] = useState<string | null>(null);
  const [speakers, setSpeakers] = useState<SpeakerEntity[]>(speakersList);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchSpeakers = async () => {
      setLoading(true);
      try {
        const response = await fetch("/api/speakers");
        if (response.ok) {
          const list = await response.json();
          // Map to correct format if needed
          const mappedList: SpeakerEntity[] = list.map((item: any) => ({
            id: item.id || item.createdAt,
            fullName: item.fullName,
            email: item.email,
            topicTitle: item.topicTitle,
            bio: item.bio,
            sessionFormat: item.sessionFormat || "Keynote",
            avRequirements: item.avRequirements || "",
            submissionType: item.submissionType || "pitch_talk",
            nomineeName: item.nomineeName || "",
            nomineeEmail: item.nomineeEmail || "",
            linkedinUrl: item.linkedinUrl || "",
            location: item.location || "Toronto, ON"
          }));
          if (mappedList.length > 0) {
            setSpeakers(mappedList);
          }
        }
      } catch (error) {
        console.error("Failed to load dynamic speaker assets:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchSpeakers();
  }, []);

  const filteredSpeakers = speakers.filter(s => 
    s.fullName.toLowerCase().includes(searchQuery.toLowerCase()) || 
    s.topicTitle.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="flex-1 flex flex-col h-full min-h-0 overflow-hidden pb-4 gap-3">
      {/* Search Input block */}
      <div className="shrink-0 relative">
        <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
          <Search size={13} className="text-slate-400" />
        </div>
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Search speakers or topics..."
          className="w-full bg-white text-slate-900 border border-slate-200/50 rounded-full pl-9 pr-4 py-2.5 text-xs font-semibold focus:outline-none focus:ring-1 focus:ring-emerald-700 transition"
        />
      </div>

      {/* Grid of speakers - list layout with custom internal vertical scroll */}
      <div className="flex-1 overflow-y-auto pr-0.5 space-y-2.5 pb-2 scrollbar-thin">
        {filteredSpeakers.length === 0 ? (
          <div className="text-center py-10 bg-white rounded-2xl border border-slate-200/50">
            <p className="text-slate-400 text-xs font-semibold">No speakers match your query.</p>
          </div>
        ) : (
          filteredSpeakers.map((speaker: SpeakerEntity) => {
            const isSelected = selectedSpeakerId === speaker.id;

            return (
              <div 
                key={speaker.id} 
                className={`bg-white rounded-2xl border transition-all duration-200 ${
                  isSelected ? "border-emerald-800 shadow-md" : "border-slate-200/60 shadow-sm"
                }`}
              >
                <div 
                  onClick={() => setSelectedSpeakerId(prev => (prev === speaker.id ? null : speaker.id))}
                  className="p-3.5 flex gap-3.5 items-start cursor-pointer select-none"
                >
                  {/* Circle initial placeholder avatar */}
                  <div className="w-10 h-10 rounded-full bg-emerald-900 text-emerald-100 flex items-center justify-center font-black text-xs shrink-0 shadow-sm">
                    {speaker.fullName.split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase()}
                  </div>

                  <div className="flex-1 space-y-0.5">
                    <div className="flex items-center justify-between">
                      <h4 className="text-xs sm:text-sm font-extrabold text-slate-950">{speaker.fullName}</h4>
                      {speaker.linkedinUrl && (
                        <a 
                          href={speaker.linkedinUrl} 
                          target="_blank" 
                          rel="noreferrer" 
                          onClick={(e) => e.stopPropagation()}
                          className="p-1 text-slate-400 hover:text-emerald-700 hover:bg-slate-50 rounded-full transition"
                        >
                          <Linkedin size={12} fill="currentColor" color="none" />
                        </a>
                      )}
                    </div>
                    
                    <span className="text-[8px] font-extrabold uppercase tracking-widest text-[#981b1b]">
                      {speaker.sessionFormat}
                    </span>

                    <p className="text-xs font-black text-slate-800 leading-snug mt-0.5">
                      {speaker.topicTitle}
                    </p>

                    <div className="flex items-center gap-1.5 text-slate-400 text-[9px] font-bold pt-0.5">
                      <MapPin size={10} className="text-slate-300" />
                      <span>{speaker.location}</span>
                    </div>
                  </div>
                </div>

                {/* Speaker Biography Sheet */}
                <AnimatePresence initial={false}>
                  {isSelected && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.2 }}
                    >
                      <div className="border-t border-slate-100 bg-slate-50/50 p-3.5 space-y-2">
                        <div className="flex items-center gap-1 text-[9px] font-extrabold uppercase tracking-widest text-emerald-900">
                          <Notebook size={11} />
                          <span>Biography Detail</span>
                        </div>
                        <p className="text-[11px] text-slate-600 leading-relaxed font-semibold">
                          {speaker.bio}
                        </p>
                        
                        <div className="flex items-center justify-between pt-1">
                          <span className="text-[9px] font-bold text-slate-400">Host Contact: {speaker.email}</span>
                          <a
                            href={speaker.linkedinUrl || "#"}
                            target="_blank"
                            rel="noreferrer"
                            className="flex items-center gap-0.5 text-[9px] text-emerald-800 font-extrabold uppercase tracking-wider hover:underline"
                          >
                            <span>LinkedIn Profile</span>
                            <ExternalLink size={9} />
                          </a>
                        </div>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
