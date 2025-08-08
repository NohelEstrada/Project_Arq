import axios from "axios";

export const useSearch = () => useState("search", () => false);

export const filter = () => useState("filter", () => axios.get(""));
