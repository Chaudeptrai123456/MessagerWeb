const links = ["Features", "Solutions", "Resources", "Pricing"];

export default function NavLinks() {
  return (
    <ul className="flex gap-8">
      {links.map((link) => (
        <li
          key={link}
          className="text-white font-medium hover:text-yellow-400 transition-colors"
        >
          {link}
        </li>
      ))}
    </ul>
  );
}
