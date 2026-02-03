import React from 'react';
import Navbar from './Navbar';

interface LayoutProps {
  children: React.ReactNode;
  showNavbar?: boolean;
  className?: string;
}

const Layout: React.FC<LayoutProps> = ({ 
  children, 
  showNavbar = true, 
  className = "" 
}) => {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {showNavbar && <Navbar />}
      
      <main className={`flex-grow ${className}`}>
        {children}
      </main>
      
      <footer className="bg-white border-t border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="text-center text-sm text-gray-500">
            <p>&copy; 2025 DoQueue</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Layout;
