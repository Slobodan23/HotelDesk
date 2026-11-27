export default function(root, opts, lib) {
  const type = root.querySelector(".mo-gt__type") || root.querySelector("h1, h2");
  if (!type) return {};
  const src = opts.image || type.dataset.image;
  if (!src) return {};
  type.style.setProperty("--mo-img", `url("${src}")`);
  type.style.setProperty("--mo-x", `${lib.clamp(+(opts.focus ?? 50), 0, 100)}%`);
  const fy = lib.clamp(+(opts.focusY ?? 62), 0, 100);
  type.style.setProperty("--mo-y", `${fy}%`);
  root.classList.add("is-live");
  if (lib.reduced()) return {};
  const travel = Math.max(0, Math.min(100, +opts.travel || 60));
  const lo = lib.clamp(fy - travel / 2, 0, 100), hi = lib.clamp(fy + travel / 2, 0, 100);
  const s = lib.scrub(root, (p) => {
    type.style.setProperty("--mo-y", `${lib.lerp(hi, lo, p).toFixed(2)}%`);
  }, { mode: "view" });
  return { destroy() {
    s.destroy();
    type.style.removeProperty("--mo-y");
  } };
}
