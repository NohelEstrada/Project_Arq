export const darkMode = () => {
  const state = useState("darkMode", () => false);

  onMounted(() => {
    if (window && window.localStorage) {
      const stored = localStorage.getItem("darkMode");
      state.value = stored ? JSON.parse(stored) : false;
    }
  });

  return state;
};

export const toggleDark = () => {
  const dark = darkMode();
  dark.value = !dark.value;
  if (typeof window !== "undefined" && window.localStorage) {
    localStorage.setItem("darkMode", JSON.stringify(dark.value));
  }
};
