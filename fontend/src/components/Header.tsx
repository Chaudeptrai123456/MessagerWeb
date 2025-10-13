import { Link } from "react-router-dom";
import Navbar from "./navbar/Navbar";

export default function Header() {
  return (
    <div
      style={{
        backgroundColor: "#282c34",
        padding: "1rem",
        color: "white",
      }}
    >
      <Navbar />
    </div>);
}
