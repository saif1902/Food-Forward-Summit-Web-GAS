import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { MapPin, Compass, Train, Flame, Home, Check, Phone, ArrowUpRight, Search, Tag, UserCheck, RefreshCw } from "lucide-react";
import { exhibitorsList } from "../data";
import { ExhibitorEntity } from "../types";

export default function LogisticsTab() {
  const [activeSubTab, setActiveSubTab] = useState<"exhibitors" | "venue">("exhibitors");
  const [exhibitorSearch, setExhibitorSearch] = useState("");
  const [selectedExhibitorId, setSelectedExhibitorId] = useState<string | null>(null);
  const [exhibitors, setExhibitors] = useState<ExhibitorEntity[]>(exhibitorsList);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchExhibitors = async () => {
      setLoading(true);
      try {
        const response = await fetch("/api/exhibitors");
        if (response.ok) {
          const raw = await response.json();
          const mapped: ExhibitorEntity[] = raw.map((item: any) => {
            let category = "Tech & Innovation";
            if (item.primarySectors && (item.primarySectors.toLowerCase().includes("sustainability") || item.primarySectors.toLowerCase().includes("packaging"))) {
              category = "Sustainability & Packaging";
            } else if (item.primarySectors && (item.primarySectors.toLowerCase().includes("supply chain") || item.primarySectors.toLowerCase().includes("automation") || item.primarySectors.toLowerCase().includes("equipment"))) {
              category = "Supply Chain & Automation";
            }
            return {
              id: item.id || `ex_${item.displayName.toLowerCase()}`,
              name: item.displayName,
              focus: item.brandsRepresented || "Primary Agriculture",
              booth: item.boothSizeConfirmed || "Kiosk B-4",
              description: item.companyDescription,
              category,
              tier: item.role === "Sponsor" ? "Platinum" : "Gold"
            };
          });
          
          if (mapped.length > 0) {
            // Keep unique items, prioritising DB models over identical static items if applicable
            const combined = [...exhibitorsList];
            mapped.forEach(dbItem => {
              if (!combined.some(existing => existing.id.toLowerCase() === dbItem.id.toLowerCase())) {
                combined.push(dbItem);
              }
            });
            setExhibitors(combined);
          }
        }
      } catch (err) {
        console.error("Failed to load dynamic exhibitors list:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchExhibitors();
  }, []);

  const filteredExhibitors = exhibitors.filter(ex => 
    ex.name.toLowerCase().includes(exhibitorSearch.toLowerCase()) ||
    ex.focus.toLowerCase().includes(exhibitorSearch.toLowerCase())
  );

  return (
    <div className="flex-1 flex flex-col h-full min-h-0 overflow-hidden pb-4 gap-3">
      {/* Selector Sub-Tabs */}
      <div className="shrink-0 flex bg-slate-200/50 p-1 rounded-xl">
        <button
          onClick={() => setActiveSubTab("exhibitors")}
          className={`flex-1 py-1.5 text-[10px] font-black uppercase tracking-wider rounded-lg transition-all ${
            activeSubTab === "exhibitors"
              ? "bg-white text-slate-900 shadow-sm"
              : "text-slate-500 hover:text-slate-900"
          }`}
        >
          Exhibitors & Booths
        </button>
        <button
          onClick={() => setActiveSubTab("venue")}
          className={`flex-1 py-1.5 text-[10px] font-black uppercase tracking-wider rounded-lg transition-all ${
            activeSubTab === "venue"
              ? "bg-white text-slate-900 shadow-sm"
              : "text-slate-500 hover:text-slate-900"
          }`}
        >
          Venue & Transit
        </button>
      </div>

      <AnimatePresence mode="wait">
        {activeSubTab === "exhibitors" ? (
          /* EXHIBITORS SUB-TAB */
          <motion.div
            key="exhibitors"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.15 }}
            className="flex-1 flex flex-col min-h-0 overflow-hidden gap-2.5"
          >
            {/* Search */}
            <div className="shrink-0 relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                <Search size={13} className="text-slate-400" />
              </div>
              <input
                type="text"
                value={exhibitorSearch}
                onChange={(e) => setExhibitorSearch(e.target.value)}
                placeholder="Search exhibitors or tech..."
                className="w-full bg-white text-slate-900 border border-slate-200/50 rounded-full pl-9 pr-4 py-2 text-xs font-semibold focus:outline-none focus:ring-1 focus:ring-emerald-700 transition"
              />
            </div>

            {/* List */}
            <div className="flex-1 overflow-y-auto pr-0.5 space-y-2 pb-2 scrollbar-thin">
              {filteredExhibitors.length === 0 ? (
                <div className="text-center py-10 bg-white rounded-2xl border border-slate-200/50">
                  <p className="text-slate-400 text-xs font-semibold">No exhibitors match your search.</p>
                </div>
              ) : (
                filteredExhibitors.map((ex: ExhibitorEntity) => {
                  const isSelected = selectedExhibitorId === ex.id;
                  return (
                    <div 
                      key={ex.id}
                      className={`bg-white rounded-2xl border transition-all duration-200 ${
                        isSelected ? "border-emerald-800 shadow-md" : "border-slate-200/60 shadow-sm"
                      }`}
                    >
                      <div
                        onClick={() => setSelectedExhibitorId(prev => (prev === ex.id ? null : ex.id))}
                        className="p-3 flex justify-between items-center cursor-pointer select-none"
                      >
                        <div className="flex items-center gap-3 pr-2 truncate">
                          <div className="text-lg w-9 h-9 bg-slate-50 border border-slate-100 rounded-xl flex items-center justify-center shadow-sm shrink-0">
                            {ex.logoAsset}
                          </div>
                          <div className="truncate">
                            <h4 className="text-xs font-black text-slate-900 flex items-center gap-1.5 truncate">
                              <span className="truncate">{ex.name}</span>
                              <span className={`text-[7px] px-1 py-0.5 rounded-full font-black uppercase shrink-0 ${
                                ex.tier === "Platinum" ? "bg-amber-100 text-amber-800" : "bg-slate-100 text-slate-600"
                              }`}>
                                {ex.tier}
                              </span>
                            </h4>
                            <span className="text-[9px] font-bold text-slate-400 block mt-0.5">{ex.boothLocation}</span>
                          </div>
                        </div>

                        <div className="text-right shrink-0">
                          <span className="text-[9px] font-extrabold px-2 py-0.5 bg-emerald-50 text-emerald-800 rounded-full">
                            {ex.track.split(" & ")[0]}
                          </span>
                        </div>
                      </div>

                      <AnimatePresence>
                        {isSelected && (
                          <motion.div
                            initial={{ height: 0, opacity: 0 }}
                            animate={{ height: "auto", opacity: 1 }}
                            exit={{ height: 0, opacity: 0 }}
                            transition={{ duration: 0.2 }}
                          >
                            <div className="border-t border-slate-100 bg-slate-50/50 p-3.5 space-y-2.5">
                              <p className="text-[11px] text-slate-650 leading-relaxed font-semibold">
                                {ex.description}
                              </p>
                              
                              <div className="bg-white p-2 text-left rounded-xl border border-slate-200/50 space-y-1">
                                <div className="text-[8px] font-extrabold uppercase tracking-widest text-[#981b1b]">Niche Tech Focus</div>
                                <p className="text-[11px] font-black text-slate-800 leading-snug">{ex.focus}</p>
                              </div>

                              <div className="flex items-center justify-between text-[9px] pt-1 font-bold text-slate-400">
                                <span>Booth: {ex.contactEmail}</span>
                                <a 
                                  href={ex.website} 
                                  target="_blank" 
                                  rel="noreferrer"
                                  className="text-emerald-805 flex items-center gap-0.5 hover:underline"
                                >
                                  <span>Website</span>
                                  <ArrowUpRight size={9} />
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
          </motion.div>
        ) : (
          /* VENUE & TRANSIT SUB-TAB */
          <motion.div
            key="venue"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.15 }}
            className="flex-1 overflow-y-auto pr-0.5 space-y-3 pb-2 scrollbar-thin"
          >
            {/* Map Placeholder Graphic */}
            <div className="relative bg-emerald-950 text-white rounded-[20px] p-4.5 overflow-hidden shadow-md">
              <div className="absolute top-0 right-0 w-24 h-24 bg-emerald-800 rounded-full blur-2xl"></div>
              <Compass size={38} className="absolute bottom-3 right-3 text-emerald-800 opacity-50" />
              
              <span className="text-[8px] font-extrabold uppercase tracking-widest text-emerald-300">Venue Coordinates</span>
              <h4 className="text-sm font-extrabold text-white mt-0.5">Milano Porta Nuova Center</h4>
              <p className="text-[11px] text-slate-200/80 mt-1 leading-normal font-semibold">
                Piazza de Castilla, 28, 20124 Milano MI, Italy
              </p>

              <div className="mt-4 flex gap-2">
                <a 
                  href="https://maps.google.com" 
                  target="_blank" 
                  rel="noreferrer" 
                  className="px-3.5 py-1.5 bg-emerald-800 hover:bg-emerald-700 text-white font-black text-[9px] uppercase tracking-wider rounded-lg flex items-center gap-0.5 cursor-pointer"
                >
                  <MapPin size={10} />
                  <span>Open Maps</span>
                </a>
                <button 
                  onClick={() => alert("Porta Nuova Center Phone: +39 02 123 4567")}
                  className="px-3.5 py-1.5 border border-emerald-700 hover:bg-emerald-900/40 text-emerald-100 font-black text-[9px] uppercase tracking-wider rounded-lg flex items-center gap-0.5 cursor-pointer"
                >
                  <Phone size={10} />
                  <span>Call Desk</span>
                </button>
              </div>
            </div>

            {/* Transit Block */}
            <div className="bg-white rounded-[20px] p-4.5 border border-slate-200/60 shadow-sm space-y-3">
              <div className="flex items-center gap-1.5">
                <Train size={13} className="text-[#981b1b]" />
                <span className="text-[9px] font-extrabold uppercase tracking-widest text-slate-400">Transit & Shuttle</span>
              </div>

              <div className="grid grid-cols-1 gap-2.5">
                <div className="flex gap-2.5 items-start">
                  <div className="w-7 h-7 rounded bg-orange-100 flex items-center justify-center shrink-0">
                    <span className="text-[9px] font-black text-orange-950">M1</span>
                  </div>
                  <div>
                    <h5 className="text-[11px] font-black text-slate-900">Metro Line M1 & M2</h5>
                    <p className="text-[10px] text-slate-500 font-semibold leading-relaxed">Gioia or Porta Garibaldi. Walk 4 minutes north.</p>
                  </div>
                </div>

                <div className="flex gap-2.5 items-start">
                  <div className="w-7 h-7 rounded bg-indigo-100 flex items-center justify-center shrink-0">
                    <span className="text-[9px] font-black text-indigo-950">LSP</span>
                  </div>
                  <div>
                    <h5 className="text-[11px] font-black text-slate-900">Summit Shuttle Coach</h5>
                    <p className="text-[10px] text-slate-500 font-semibold leading-relaxed">Departs Milano Centrale every 15 mins directly.</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Hotel Guide */}
            <div className="bg-white rounded-[20px] p-4.5 border border-slate-200/60 shadow-sm space-y-2">
              <div className="flex items-center gap-1.5">
                <Home size={13} className="text-emerald-800" />
                <span className="text-[9px] font-extrabold uppercase tracking-widest text-slate-400">Hotel Partners</span>
              </div>
              <ul className="space-y-1.5 text-[10px] text-slate-600 font-semibold">
                <li className="flex justify-between items-center py-1 border-b border-slate-100">
                  <div>
                    <div className="font-extrabold text-slate-950">Milano Valesca</div>
                    <div className="text-[9px] text-slate-450">0.3km away • Partner Rate</div>
                  </div>
                  <span className="text-emerald-800 text-[9px] font-black">Save 15%</span>
                </li>
                <li className="flex justify-between items-center py-1">
                  <div>
                    <div className="font-extrabold text-slate-950">Residenza Garibaldi</div>
                    <div className="text-[9px] text-slate-450">0.8km away • Standard Partner</div>
                  </div>
                  <span className="text-emerald-800 text-[9px] font-black">Save 10%</span>
                </li>
              </ul>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
