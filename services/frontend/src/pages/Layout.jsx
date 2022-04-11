import { Outlet, Link } from "react-router-dom";
import   '../styles/Layout.css';
import logo from '../assets/neogenia-logo1.png'

const Layout = () => {
  return (
    <>
      <nav>
        <ul>
        <li>
            <Link to="/auth/login">  <button id="text"> Sign-in</button> </Link>
            </li>
           <li>
            <Link to="/auth/sign-up"> <button id="text">Sign-up</button>  </Link>
            </li>
            
          
        
    
        </ul>
      </nav>

      <Outlet />
    </>
  )
};

export default Layout;