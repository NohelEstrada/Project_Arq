<script setup lang="ts">
const toggleSidebar: Ref<boolean> = useToggleSidebar();
const currentId: Ref<number> = ref(0);
const categories = useCategories();
const showSubcategories: Ref<boolean[]> = ref([]);
const search: Ref<boolean> = useSearch();
const edit: Ref<boolean> = useEdit();
const router = useRoute();
const navigate = useRouter();
const dark = darkMode();

function toggle(id: number): void {
  if (currentId.value === id) {
    toggleSidebar.value = false;
    currentId.value = 0;
  } else {
    toggleSidebar.value = true;
    currentId.value = id;
  }
}

interface SidebarItem {
  id: number;
  show: () => boolean;
  conditional: () => boolean;
  click: () => void;
  showDescription: boolean;
  dragging: boolean;
  over: boolean;
  description: string;
  iconPath: string | (() => string);
}

const sidebarItems: Ref<SidebarItem[]> = ref([
  {
    id: 1,
    show: () => {
      let show = false;
      router.path === "/medicines" ? (show = true) : null;
      router.path === "/prescription" ? (show = true) : null;
      router.path === "/hospital" ? (show = true) : null;
      router.path === "/pharmacy" ? (show = true) : null;
      router.path === "/policies" ? (show = true) : null;
      router.path === "/services" ? (show = true) : null;
      router.path === "/users" ? (show = true) : null;
      router.path === "/transactions" ? (show = true) : null;
      return show;
    },
    conditional: () => search.value,
    click: () => {
      search.value = !search.value;
    },
    showDescription: false,
    dragging: false,
    over: false,
    description: "Search for what you want.",
    iconPath:
      "M784-120 532-372q-30 24-69 38t-83 14q-109 0-184.5-75.5T120-580q0-109 75.5-184.5T380-840q109 0 184.5 75.5T640-580q0 44-14 83t-38 69l252 252-56 56ZM380-400q75 0 127.5-52.5T560-580q0-75-52.5-127.5T380-760q-75 0-127.5 52.5T200-580q0 75 52.5 127.5T380-400Z",
  },
  {
    id: 2,
    show: () => useAuth("read", "signup").value,
    conditional: () => router.path === "/signup",
    click: () => navigate.push("/signup"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "Sign up for a new account",
    iconPath:
      "M720-400v-120H600v-80h120v-120h80v120h120v80H800v120h-80Zm-360-80q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47ZM40-160v-112q0-34 17.5-62.5T104-378q62-31 126-46.5T360-440q66 0 130 15.5T616-378q29 15 46.5 43.5T680-272v112H40Zm80-80h480v-32q0-11-5.5-20T580-306q-54-27-109-40.5T360-360q-56 0-111 13.5T140-306q-9 5-14.5 14t-5.5 20v32Zm240-320q33 0 56.5-23.5T440-640q0-33-23.5-56.5T360-720q-33 0-56.5 23.5T280-640q0 33 23.5 56.5T360-560Zm0-80Zm0 400Z",
  },
  {
    id: 3,
    show: () => useAuth("read", "login").value,
    conditional: () => router.path === "/login",
    click: () => navigate.push("/login"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "Enter your credentials to login.",
    iconPath:
      "M480-120v-80h280v-560H480v-80h280q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H480Zm-80-160-55-58 102-102H120v-80h327L345-622l55-58 200 200-200 200Z",
  },
  {
    id: 4,
    show: () => useAuth("read", "profile").value,
    conditional: () => router.path === "/profile",
    click: () => navigate.push("/profile"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "View and edit your profile.",
    iconPath:
      "M480-480q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47ZM160-160v-112q0-34 17.5-62.5T224-378q62-31 126-46.5T480-440q66 0 130 15.5T736-378q29 15 46.5 43.5T800-272v112H160Zm80-80h480v-32q0-11-5.5-20T700-306q-54-27-109-40.5T480-360q-56 0-111 13.5T260-306q-9 5-14.5 14t-5.5 20v32Zm240-320q33 0 56.5-23.5T560-640q0-33-23.5-56.5T480-720q-33 0-56.5 23.5T400-640q0 33 23.5 56.5T480-560Zm0-80Zm0 400Z",
  },
  {
    id: 5,
    show: () => useAuth("read", "logout").value,
    conditional: () => false,
    click: () => {
      setUser("guest");
      localStorage.removeItem("profile");
      navigate.push("/");
    },
    showDescription: false,
    dragging: false,
    over: false,
    description: "Close session and logout.",
    iconPath:
      "M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h280v80H200v560h280v80H200Zm440-160-55-58 102-102H360v-80h327L585-622l55-58 200 200-200 200Z",
  },
  {
    id: 6,
    show: () => useAuth("read", "policies").value,
    conditional: () => router.path === "/policies",
    click: () => navigate.push("/policies"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "View our policies and its details.",
    iconPath:
      "M320-240h320v-80H320v80Zm0-160h320v-80H320v80ZM240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5T240-880h320l240 240v480q0 33-23.5 56.5T720-80H240Zm280-520v-200H240v640h480v-440H520ZM240-800v200-200 640-640Z",
  },
  {
    id: 7,
    show: () => useAuth("read", "calendar").value,
    conditional: () => router.path === "/calendar",
    click: () => navigate.push("/calendar"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "View your cites and create new ones.",
    iconPath:
      "M200-80q-33 0-56.5-23.5T120-160v-560q0-33 23.5-56.5T200-800h40v-80h80v80h320v-80h80v80h40q33 0 56.5 23.5T840-720v560q0 33-23.5 56.5T760-80H200Zm0-80h560v-400H200v400Zm0-480h560v-80H200v80Zm0 0v-80 80Zm280 240q-17 0-28.5-11.5T440-440q0-17 11.5-28.5T480-480q17 0 28.5 11.5T520-440q0 17-11.5 28.5T480-400Zm-160 0q-17 0-28.5-11.5T280-440q0-17 11.5-28.5T320-480q17 0 28.5 11.5T360-440q0 17-11.5 28.5T320-400Zm320 0q-17 0-28.5-11.5T600-440q0-17 11.5-28.5T640-480q17 0 28.5 11.5T680-440q0 17-11.5 28.5T640-400ZM480-240q-17 0-28.5-11.5T440-280q0-17 11.5-28.5T480-320q17 0 28.5 11.5T520-280q0 17-11.5 28.5T480-240Zm-160 0q-17 0-28.5-11.5T280-280q0-17 11.5-28.5T320-320q17 0 28.5 11.5T360-280q0 17-11.5 28.5T320-240Zm320 0q-17 0-28.5-11.5T600-280q0-17 11.5-28.5T640-320q17 0 28.5 11.5T680-280q0 17-11.5 28.5T640-240Z",
  },
  {
    id: 8,
    show: () => useAuth("read", "hospital").value,
    conditional: () => router.path === "/hospital",
    click: () => navigate.push("/hospital"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "View hospitals and associate new ones.",
    iconPath:
      "M420-280h120v-140h140v-120H540v-140H420v140H280v120h140v140ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z",
  },
  {
    id: 9,
    show: () => useAuth("read", "pharmacy").value,
    conditional: () => router.path === "/pharmacy",
    click: () => navigate.push("/pharmacy"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "View pharmacies and associate new ones.",
    iconPath:
      "M420-260h120v-100h100v-120H540v-100H420v100H320v120h100v100ZM280-120q-33 0-56.5-23.5T200-200v-440q0-33 23.5-56.5T280-720h400q33 0 56.5 23.5T760-640v440q0 33-23.5 56.5T680-120H280Zm0-80h400v-440H280v440Zm-40-560v-80h480v80H240Zm40 120v440-440Z",
  },
  {
    id: 10,
    show: () => useAuth("read", "prescription").value,
    conditional: () => router.path === "/prescription",
    click: () => navigate.push("/prescription"),
    showDescription: false,
    dragging: false,
    over: false,
    description:
      "View send prescriptions from hospitals, pendding for approval.",
    iconPath:
      "m678-134 46-46-64-64-46 46q-14 14-14 32t14 32q14 14 32 14t32-14Zm102-102 46-46q14-14 14-32t-14-32q-14-14-32-14t-32 14l-46 46 64 64ZM735-77q-37 37-89 37t-89-37q-37-37-37-89t37-89l148-148q37-37 89-37t89 37q37 37 37 89t-37 89L735-77ZM200-200v-560 560Zm0 80q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h168q13-36 43.5-58t68.5-22q38 0 68.5 22t43.5 58h168q33 0 56.5 23.5T840-760v245q-20-5-40-5t-40 3v-243H200v560h243q-3 20-3 40t5 40H200Zm280-670q13 0 21.5-8.5T510-820q0-13-8.5-21.5T480-850q-13 0-21.5 8.5T450-820q0 13 8.5 21.5T480-790ZM280-600v-80h400v80H280Zm0 160v-80h400v34q-8 5-15.5 11.5T649-460l-20 20H280Zm0 160v-80h269l-49 49q-8 8-14.5 15.5T474-280H280Z",
  },
  {
    id: 11,
    show: () => useAuth("read", "services").value,
    conditional: () => router.path === "/services",
    click: () => navigate.push("/services"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "See all the services available.",
    iconPath:
      "M160-80q-33 0-56.5-23.5T80-160v-480q0-33 23.5-56.5T160-720h160v-80q0-33 23.5-56.5T400-880h160q33 0 56.5 23.5T640-800v80h160q33 0 56.5 23.5T880-640v480q0 33-23.5 56.5T800-80H160Zm0-80h640v-480H160v480Zm240-560h160v-80H400v80ZM160-160v-480 480Zm280-200v120h80v-120h120v-80H520v-120h-80v120H320v80h120Z",
  },
  {
    id: 12,
    show: () => useAuth("read", "medicines").value,
    conditional: () => router.path === "/medicines",
    click: () => navigate.push("/medicines"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "Consult the medicines available.",
    iconPath:
      "m320-60-80-60v-160h-40q-33 0-56.5-23.5T120-360v-300q-17 0-28.5-11.5T80-700q0-17 11.5-28.5T120-740h120v-60h-20q-17 0-28.5-11.5T180-840q0-17 11.5-28.5T220-880h120q17 0 28.5 11.5T380-840q0 17-11.5 28.5T340-800h-20v60h120q17 0 28.5 11.5T480-700q0 17-11.5 28.5T440-660v300q0 33-23.5 56.5T360-280h-40v220ZM200-360h160v-60h-70q-12 0-21-9t-9-21q0-12 9-21t21-9h70v-60h-70q-12 0-21-9t-9-21q0-12 9-21t21-9h70v-60H200v300ZM600-80q-33 0-56.5-23.5T520-160v-260q0-29 10-48t21-33q11-14 20-22.5t9-16.5v-20q-17 0-28.5-11.5T540-600q0-17 11.5-28.5T580-640h200q17 0 28.5 11.5T820-600q0 17-11.5 28.5T780-560v20q0 8 10 18t22 24q11 14 19.5 33t8.5 45v260q0 33-23.5 56.5T760-80H600Zm0-320h160v-20q0-15-9-26t-20-24q-11-13-21-29t-10-41v-20h-40v20q0 24-9.5 40T630-471q-11 13-20.5 24.5T600-420v20Zm0 120h160v-60H600v60Zm0 120h160v-60H600v60Zm0-120h160-160Z",
  },
  {
    id: 13,
    show: () => useAuth("read", "transactions").value,
    conditional: () => router.path === "/transactions",
    click: () => navigate.push("/transactions"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "See all the current transactions.",
    iconPath:
      "M240-80q-50 0-85-35t-35-85v-120h120v-560h600v680q0 50-35 85t-85 35H240Zm480-80q17 0 28.5-11.5T760-200v-600H320v480h360v120q0 17 11.5 28.5T720-160ZM360-600v-80h360v80H360Zm0 120v-80h360v80H360ZM240-160h360v-80H200v40q0 17 11.5 28.5T240-160Zm0 0h-40 400-360Z",
  },
  {
    id: 14,
    show: () => {
      let show = false;
      router.path === "/services" ? show = true : show = false;
      if (!show) {
        clearCategories();
      }
      return show;
    },
    conditional: () => currentId.value === 3,
    click: () => toggle(3),
    showDescription: false,
    dragging: false,
    over: false,
    description: "Filter by category.",
    iconPath:
      "M440-160q-17 0-28.5-11.5T400-200v-240L168-736q-15-20-4.5-42t36.5-22h560q26 0 36.5 22t-4.5 42L560-440v240q0 17-11.5 28.5T520-160h-80Zm40-308 198-252H282l198 252Zm0 0Z",
  },
  {
    id: 15,
    show: () => useAuth("read", "edit").value,
    conditional: () => edit.value,
    click: () => {
      edit.value = !edit.value;
    },
    showDescription: false,
    dragging: false,
    over: false,
    description: "Change what you want.",
    iconPath:
      "M200-200h57l391-391-57-57-391 391v57Zm-80 80v-170l528-527q12-11 26.5-17t30.5-6q16 0 31 6t26 18l55 56q12 11 17.5 26t5.5 30q0 16-5.5 30.5T817-647L290-120H120Zm640-584-56-56 56 56Zm-141 85-28-29 57 57-29-28Z",
  },
  {
    id: 16,
    show: () => true,
    conditional: () => dark.value,
    click: () => toggleDark(),
    showDescription: false,
    dragging: false,
    over: false,
    description: "Toggle dark mode, relief your eyes.",
    iconPath: () =>
      !dark.value
        ? "M480-360q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35Zm0 80q-83 0-141.5-58.5T280-480q0-83 58.5-141.5T480-680q83 0 141.5 58.5T680-480q0 83-58.5 141.5T480-280ZM200-440H40v-80h160v80Zm720 0H760v-80h160v80ZM440-760v-160h80v160h-80Zm0 720v-160h80v160h-80ZM256-650l-101-97 57-59 96 100-52 56Zm492 496-97-101 53-55 101 97-57 59Zm-98-550 97-101 59 57-100 96-56-52ZM154-212l101-97 55 53-97 101-59-57Zm326-268Z"
        : "M480-120q-150 0-255-105T120-480q0-150 105-255t255-105q14 0 27.5 1t26.5 3q-41 29-65.5 75.5T444-660q0 90 63 153t153 63q55 0 101-24.5t75-65.5q2 13 3 26.5t1 27.5q0 150-105 255T480-120Zm0-80q88 0 158-48.5T740-375q-20 5-40 8t-40 3q-123 0-209.5-86.5T364-660q0-20 3-40t8-40q-78 32-126.5 102T200-480q0 116 82 198t198 82Zm-10-270Z",
  },
  {
    id: 17,
    show: () => useAuth("read", "moderation").value,
    conditional: () => router.path === "/moderation",
    click: () => navigate.push("/moderation"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "Apply the changes to the modules.",
    iconPath:
      "M440-82q-76-8-141.5-41.5t-114-87Q136-264 108-333T80-480q0-91 36.5-168T216-780h-96v-80h240v240h-80v-109q-55 44-87.5 108.5T160-480q0 123 80.5 212.5T440-163v81Zm-17-214L254-466l56-56 113 113 227-227 56 57-283 283Zm177 196v-240h80v109q55-45 87.5-109T800-480q0-123-80.5-212.5T520-797v-81q152 15 256 128t104 270q0 91-36.5 168T744-180h96v80H600Z",
  },
  {
    id: 18,
    show: () => useAuth("read", "moderation").value,
    conditional: () => router.path === "/users",
    click: () => navigate.push("/users"),
    showDescription: false,
    dragging: false,
    over: false,
    description: "Manage the users.",
    iconPath:
      "M40-160v-112q0-34 17.5-62.5T104-378q62-31 126-46.5T360-440q66 0 130 15.5T616-378q29 15 46.5 43.5T680-272v112H40Zm720 0v-120q0-44-24.5-84.5T666-434q51 6 96 20.5t84 35.5q36 20 55 44.5t19 53.5v120H760ZM360-480q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47Zm400-160q0 66-47 113t-113 47q-11 0-28-2.5t-28-5.5q27-32 41.5-71t14.5-81q0-42-14.5-81T544-792q14-5 28-6.5t28-1.5q66 0 113 47t47 113ZM120-240h480v-32q0-11-5.5-20T580-306q-54-27-109-40.5T360-360q-56 0-111 13.5T140-306q-9 5-14.5 14t-5.5 20v32Zm240-320q33 0 56.5-23.5T440-640q0-33-23.5-56.5T360-720q-33 0-56.5 23.5T280-640q0 33 23.5 56.5T360-560Zm0 320Zm0-400Z",
  },
]);

let ids: number[] = [];
onMounted((): void => {
  const storedIds = localStorage.getItem("ids");
  if (!storedIds) {
    ids = [];
    sidebarItems.value.forEach((element: SidebarItem) => ids.push(element.id));
    localStorage.setItem("ids", JSON.stringify(ids));
  } else {
    ids = JSON.parse(storedIds);
  }
  categories.value.forEach(() => {
    showSubcategories.value.push(true);
  });
  let tempItems: SidebarItem[] = [];
  ids.forEach((id) => {
    const found = sidebarItems.value.find((element) => element.id === id);
    if (found) {
      tempItems.push(found);
    }
  });
  sidebarItems.value = tempItems;
});

watch(sidebarItems.value, () => {
  ids = [];
  sidebarItems.value.forEach((element: SidebarItem) => ids.push(element.id));
  localStorage.setItem("ids", JSON.stringify(ids));
});

watch(getCurrentCategory(), ()=>{
  console.log(getCurrentCategory().value);
})
const currentCategory = getCurrentCategory();
</script>
<template>
  <main class="bg-background border-b-secondary flex flex-col border-b-1 py-2">
    <NuxtLink to="/" class="mb-6 flex justify-center">
      <img
        class="border-h-color flex h-12 rounded-full border-2"
        src="/logo.png"
        alt="company logo"
      />
    </NuxtLink>
    <div v-for="item in sidebarItems" class="flex">
      <div class="flex">
        <button
          @dragover.prevent="
            () => {
              sidebarItems.forEach((element) => {
                element.over = false;
              });
              item.over = true;
            }
          "
          @dragstart="item.dragging = true"
          @dragend="
            () => {
              let hoverIndex: number = 0;
              let draggedIndex: number = 0;
              sidebarItems.forEach((element, index) => {
                if (element.over) {
                  hoverIndex = index;
                }
                if (element.dragging) {
                  draggedIndex = index;
                }
              });

              [sidebarItems[draggedIndex], sidebarItems[hoverIndex]] = [
                sidebarItems[hoverIndex],
                sidebarItems[draggedIndex],
              ];

              sidebarItems.forEach((element) => {
                element.dragging = false;
                element.over = false;
              });
            }
          "
          v-if="item.show()"
          @mouseover="item.showDescription = true"
          @mouseleave="item.showDescription = false"
          @click="() => item.click()"
          :class="[
            'px-2 py-2',
            item.conditional()
              ? 'bg-accent hover:bg-h-accent'
              : 'hover:bg-h-background',
          ]"
          draggable="true"
        >
          <svg
            :class="[
              item.conditional() ? 'text-light dark:text-dark' : 'text-primary',
            ]"
            xmlns="http://www.w3.org/2000/svg"
            height="40px"
            viewBox="0 -960 960 960"
            width="40px"
            fill="currentColor"
          >
            <path
              :d="
                typeof item.iconPath === 'function'
                  ? item.iconPath()
                  : item.iconPath
              "
            />
          </svg>
          <Description :name="item.description" :show="item.showDescription" />
        </button>
      </div>
    </div>
  </main>
  <section
    class="border-b-secondary min-w-[200px] justify-center border-b-1 border-l-1 border-l-gray-400 p-2 max-sm:w-full sm:w-[20%]"
    v-if="toggleSidebar && currentId === 3 && router.path === '/services'"
  >
    <div
      class="flex max-h-[calc(100vh-8rem)] flex-col overflow-auto max-sm:items-center"
    >
      <div
        class="text-primary hover:text-h-accent my-4 ms-2 mb-4 font-bold"
        v-for="(category, catIndex) in categories"
        :key="category.id"
      >
        <button
          class="mb-2 flex"
          @click="
            () => {
              showSubcategories[catIndex] = !showSubcategories[catIndex];
              selectedCategory(category.id, null);
              console.log(currentCategory);
            }
          "
        >
          {{ category.name }}
          <svg
            v-if="showSubcategories[catIndex]"
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill="currentColor"
          >
            <path d="M480-360 280-560h400L480-360Z" />
          </svg>
          <svg
            v-if="!showSubcategories[catIndex]"
            class="ms-2"
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill="currentColor"
          >
            <path d="m280-400 200-200 200 200H280Z" />
          </svg>
        </button>
        <button
          @click="() => {selectedCategory(category.id, subcategory.id); 
            }"
          v-for="subcategory in category.subcategories"
          :key="subcategory.id"
          class="text-secondary hover:text-h-accent ms-2 border-s-2 ps-2 font-semibold"
          v-if="showSubcategories[catIndex]"
        >
          {{ subcategory.name }}
        </button>
      </div>
    </div>
  </section>
</template>
