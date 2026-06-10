import { useState } from "react";
import { motion, AnimatePresence } from "motion/react";
import { Clock, MapPin, Tag, Bookmark, Check, ChevronDown, ChevronUp, Download, Sparkles } from "lucide-react";
import { initialSessions } from "../data";
import { AgendaSession } from "../types";

interface ScheduleTabProps {
  bookmarkedIds: string[];
  onToggleBookmark: (id: string) => void;
}

export default function ScheduleTab({ bookmarkedIds, onToggleBookmark }: ScheduleTabProps) {
  const [selectedTrack, setSelectedTrack] = useState<string>("All");
  const [expandedSessionId, setExpandedSessionId] = useState<string | null>(null);

  // Available unique tracks
  const tracks = ["All", "Tech & Innovation", "Sustainability & Packaging", "Supply Chain & Automation", "Consumer & Regulatory"];

  // Filter sessions
  const filteredSessions = selectedTrack === "All"
    ? initialSessions
    : initialSessions.filter(s => s.track === selectedTrack);

  const toggleExpand = (id: string) => {
    setExpandedSessionId(prev => (prev === id ? null : id));
  };

  return (
    <div className="flex-1 flex flex-col h-full min-h-0 overflow-hidden pb-4 gap-3">
      {/* 1. Category Pill Filter Scroll View (Horizontal Touch Scroll support) - Reduced margin */}
      <div className="shrink-0 bg-slate-100 py-1.5 -mx-5 px-5 overflow-x-auto scrollbar-none flex gap-2">
        {tracks.map((track) => {
          const isActive = selectedTrack === track;
          return (
            <button
              key={track}
              onClick={() => setSelectedTrack(track)}
              className={`whitespace-nowrap px-3.5 py-1.5 text-[10px] font-black uppercase tracking-wider rounded-full transition-all duration-150 cursor-pointer ${
                isActive
                  ? "bg-emerald-900 text-white shadow-sm"
                  : "bg-white text-slate-500 hover:text-slate-900 border border-slate-200/50"
              }`}
            >
              {track}
            </button>
          );
        })}
      </div>

      {/* 2. Interactive Timeline Feed with isolated vertical scrolling */}
      <div className="flex-1 overflow-y-auto pr-0.5 space-y-2.5 pb-2 scrollbar-thin">
        {filteredSessions.length === 0 ? (
          <div className="text-center py-10 bg-white rounded-2xl border border-slate-200/50">
            <p className="text-slate-400 text-xs font-semibold">No specialized events in this track.</p>
          </div>
        ) : (
          filteredSessions.map((session: AgendaSession) => {
            const isBookmarked = bookmarkedIds.includes(session.id);
            const isExpanded = expandedSessionId === session.id;

            return (
              <motion.div
                key={session.id}
                layout="position"
                className={`bg-white rounded-2xl border transition-all duration-200 overflow-hidden ${
                  isExpanded ? "border-emerald-800 shadow-md ring-1 ring-emerald-800/10" : "border-slate-200/60 shadow-sm"
                }`}
              >
                {/* Session Summary Card Row */}
                <div 
                  onClick={() => toggleExpand(session.id)}
                  className="p-3.5 flex gap-3 items-start justify-between cursor-pointer select-none"
                >
                  <div className="space-y-1 flex-1 pr-2">
                    {/* Time Frame Badge */}
                    <div className="flex items-center gap-1 text-slate-400 font-mono text-[9px] font-extrabold">
                      <Clock size={10} className="text-emerald-700" />
                      <span>{session.startTime} - {session.endTime}</span>
                    </div>

                    <h4 className="text-xs sm:text-sm font-extrabold text-slate-950 leading-snug">
                      {session.title}
                    </h4>

                    {/* Meta badges: Speaker & Track & Room */}
                    <div className="flex flex-wrap items-center gap-1.5 mt-1">
                      <span className="text-[9px] font-bold text-slate-650">
                        {session.speaker}
                      </span>
                      <span className="text-[8px] px-1 px-0.5 bg-slate-100 text-slate-500 rounded font-extrabold uppercase tracking-wide">
                        {session.location.split(" (")[0]}
                      </span>
                    </div>
                  </div>

                  {/* Right Actions Block */}
                  <div className="flex items-center gap-0.5 self-center" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => onToggleBookmark(session.id)}
                      className={`p-2 rounded-full border transition active:scale-95 cursor-pointer ${
                        isBookmarked 
                          ? "bg-emerald-50 border-emerald-200 text-emerald-800"
                          : "border-slate-200 text-slate-400 hover:text-slate-600 hover:bg-slate-50"
                      }`}
                    >
                      <Bookmark size={11} fill={isBookmarked ? "currentColor" : "none"} />
                    </button>
                    <button
                      onClick={() => toggleExpand(session.id)}
                      className="p-2 text-slate-400 hover:text-slate-600 transition"
                    >
                      {isExpanded ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
                    </button>
                  </div>
                </div>

                {/* Expanded Session Panel details */}
                <AnimatePresence initial={false}>
                  {isExpanded && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.2, ease: "easeInOut" }}
                    >
                      <div className="border-t border-slate-100 bg-slate-50/50 p-3.5 space-y-3">
                        <div className="text-[11px] text-slate-600 leading-relaxed font-semibold">
                          {session.description}
                        </div>

                        {/* Speaker Full Profile Box */}
                        <div className="bg-white p-2.5 rounded-xl border border-slate-200/50 flex flex-col gap-0.5">
                          <span className="text-[8px] font-extrabold text-emerald-805 uppercase tracking-widest block">Speaker Host</span>
                          <span className="text-xs font-black text-slate-900">{session.speaker}</span>
                          <span className="text-[9px] text-slate-500 leading-none">{session.speakerRole}</span>
                        </div>

                        {/* Venue Detail / Track Info */}
                        <div className="flex items-center gap-3 text-[10px] text-slate-500 font-bold bg-white p-2.5 rounded-xl border border-slate-200/50 justify-between">
                          <div className="flex items-center gap-1">
                            <MapPin size={11} className="text-amber-500" />
                            <span>{session.location}</span>
                          </div>
                          <div className="flex items-center gap-1 opacity-80 shrink-0">
                            <Tag size={10} />
                            <span>{session.track}</span>
                          </div>
                        </div>

                        {/* Attachment Download Link (Simulation) */}
                        {session.attachmentUrl && (
                          <div className="flex items-center justify-between p-2 px-3 bg-emerald-50 rounded-xl border border-emerald-100/60">
                            <div className="flex items-center gap-1.5">
                              <Sparkles size={11} className="text-emerald-700 animate-pulse" />
                              <span className="text-[9px] font-extrabold font-mono text-emerald-900 truncate max-w-[170px]">
                                {session.attachmentUrl}
                              </span>
                            </div>
                            <button
                              onClick={() => alert(`Simulated Download of document: ${session.attachmentUrl}`)}
                              className="flex items-center gap-1 p-1 px-2 bg-emerald-800 text-white rounded-lg text-[8px] font-black uppercase tracking-wider cursor-pointer active:scale-95"
                            >
                              <Download size={9} />
                              <span>Get Doc</span>
                            </button>
                          </div>
                        )}
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </motion.div>
            );
          })
        )}
      </div>
    </div>
  );
}
