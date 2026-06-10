import { useState } from "react";
import { motion, AnimatePresence } from "motion/react";
import LoginForm, { UserSession } from "./components/LoginForm";
import DashboardTab from "./components/DashboardTab";
import ScheduleTab from "./components/ScheduleTab";
import SpeakersTab from "./components/SpeakersTab";
import LogisticsTab from "./components/LogisticsTab";
import TicketPassModal from "./components/TicketPassModal";
import { LayoutDashboard, Calendar, Users, Map, Ticket, LogOut, Sparkles } from "lucide-react";

type TabType = "dashboard" | "schedule" | "speakers" | "logistics";

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentUserEmail, setCurrentUserEmail] = useState("cognico@nleats.com");
  const [userSession, setUserSession] = useState<UserSession | null>(null);
  
  // Mobile / Tablet Tab Navigation State
  const [activeTab, setActiveTab] = useState<TabType>("dashboard");
  
  // Registration and Ticket Pass state
  const [isRegistered, setIsRegistered] = useState(false);
  const [isTicketOpen, setIsTicketOpen] = useState(false);
  
  // Saved/Bookmarked Session IDs
  const [bookmarkedSessionIds, setBookmarkedSessionIds] = useState<string[]>(["session_1"]);

  // Handle Login event from child with full UUID auth session
  const handleLogin = (session: UserSession) => {
    setUserSession(session);
    setCurrentUserEmail(session.email || "exhibitor@nleats.com");
    setIsRegistered(true); // Pre-registered since they exist in DB
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setUserSession(null);
    setIsRegistered(false);
    setActiveTab("dashboard");
  };

  const toggleBookmark = (id: string) => {
    setBookmarkedSessionIds(prev => 
      prev.includes(id) ? prev.filter(item => item !== id) : [...prev, id]
    );
  };

  // If not authenticated, render our custom login form matching the PNG mockup
  if (!isAuthenticated) {
    return <LoginForm onLogin={handleLogin} />;
  }

  return (
    <div className="h-screen w-screen bg-slate-105 text-slate-900 font-sans flex flex-col items-center justify-center overflow-hidden">
      
      {/* Outer App Frame - Restricted max-width to optimize beautifully on standard mobile sizes and tablets */}
      <div className="w-full max-w-md h-full max-h-[100dvh] sm:h-[840px] sm:max-h-[90vh] bg-slate-50 flex flex-col relative shadow-2xl sm:rounded-[40px] sm:border sm:border-slate-200/50 overflow-hidden">
        
        {/* 1. TOP MOBILE HEADER */}
        <header className="shrink-0 bg-white/95 backdrop-blur-md z-30 px-6 py-4 border-b border-sidebar-border border-slate-200/50 flex justify-between items-center h-16">
          <div className="flex items-center gap-1.5 select-none">
            {/* Custom Mini Brand Logo */}
            <div className="w-7 h-7 bg-emerald-900 rounded-lg flex items-center justify-center font-black text-[10px] text-white tracking-widest leading-none shadow-sm">
              FF
            </div>
            <div className="text-sm font-black tracking-tighter text-emerald-990 uppercase mt-0.5">
              Food Forward <span className="text-emerald-600 font-bold">2026</span>
            </div>
          </div>

          {/* Quick Profile / Log Out Trigger */}
          <div className="flex items-center gap-2.5">
            <button
              onClick={() => setIsTicketOpen(true)}
              className="relative p-2 hover:bg-slate-100 rounded-full text-emerald-850 cursor-pointer active:scale-95 transition"
              title="View Ticket Pass"
            >
              <Ticket size={16} />
              {isRegistered && (
                <span className="absolute top-1 right-1 w-2.5 h-2.5 bg-amber-400 rounded-full border-2 border-white" />
              )}
            </button>
            <button
              onClick={handleLogout}
              className="p-2 text-slate-400 hover:text-[#981b1b] hover:bg-red-50 rounded-full transition cursor-pointer active:scale-95"
              title="Logout"
            >
              <LogOut size={16} />
            </button>
          </div>
        </header>

        {/* 2. DYNAMIC CONTENT MAIN AREA (Scroll-locked inner contents with flexible layout) */}
        <main className="flex-1 overflow-hidden px-5 pt-3 pb-24 flex flex-col min-h-0">
          <AnimatePresence mode="wait">
            <motion.div
              key={activeTab}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              transition={{ duration: 0.15 }}
              className="flex-1 flex flex-col min-h-0"
            >
              {activeTab === "dashboard" && (
                <DashboardTab 
                  onOpenTicket={() => setIsTicketOpen(true)} 
                  isRegistered={isRegistered} 
                  userName={userSession?.displayName}
                  userRole={userSession?.role}
                  userSession={userSession}
                />
              )}

              {activeTab === "schedule" && (
                <ScheduleTab 
                  bookmarkedIds={bookmarkedSessionIds} 
                  onToggleBookmark={toggleBookmark} 
                />
              )}

              {activeTab === "speakers" && (
                <SpeakersTab />
              )}

              {activeTab === "logistics" && (
                <LogisticsTab />
              )}
            </motion.div>
          </AnimatePresence>
        </main>

        {/* 3. PERSISTENT MOBILE BOTTOM TAB NAVIGATION */}
        <nav className="absolute bottom-0 inset-x-0 bg-white border-t border-slate-200/85 py-2 px-4 flex justify-around items-center z-40 shadow-[0_-4px_16px_rgba(0,0,0,0.03)] pb-safe">
          {[
            { id: "dashboard", label: "Home", icon: LayoutDashboard },
            { id: "schedule", label: "Schedule", icon: Calendar },
            { id: "speakers", label: "Speakers", icon: Users },
            { id: "logistics", label: "Venue", icon: Map }
          ].map((tab) => {
            const IconComponent = tab.icon;
            const isActive = activeTab === tab.id;

            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as TabType)}
                className="flex flex-col items-center justify-center py-1.5 px-3 rounded-2xl relative transition-all duration-150 cursor-pointer touch-manipulation active:scale-95 group"
              >
                {/* Active Tab Background Pill */}
                {isActive && (
                  <motion.div
                    layoutId="activeTabPill"
                    className="absolute inset-0 bg-emerald-50 rounded-xl -z-10"
                    transition={{ type: "spring", stiffness: 380, damping: 30 }}
                  />
                )}

                <IconComponent 
                  size={18} 
                  strokeWidth={isActive ? 2.5 : 2}
                  className={`transition-colors duration-150 ${
                    isActive ? "text-emerald-800" : "text-slate-400 group-hover:text-slate-600"
                  }`} 
                />
                
                <span className={`text-[9px] font-extrabold mt-1 tracking-wide uppercase transition-colors duration-150 ${
                  isActive ? "text-emerald-950 font-black" : "text-slate-400 group-hover:text-slate-500"
                }`}>
                  {tab.label}
                </span>
              </button>
            );
          })}
        </nav>

        {/* 4. WALLET PASS TICKET BOTTOM-SHEET / MODAL */}
        <AnimatePresence>
          {isTicketOpen && (
            <TicketPassModal 
              isOpen={isTicketOpen}
              onClose={() => setIsTicketOpen(false)}
              isRegistered={isRegistered}
              onRegister={() => setIsRegistered(true)}
              userEmail={currentUserEmail}
              userName={userSession?.displayName}
              userRole={userSession?.role}
            />
          )}
        </AnimatePresence>

      </div>
    </div>
  );
}
