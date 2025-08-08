interface Permission {
  role: Role[];
  action: Action;
  resource: Resource;
  additional?: () => boolean[];
}
type Role = "admin" | "user" | "guest" | "employee" | "extern";
type Action = "create" | "read" | "update" | "delete";
type Resource =
  | "signup"
  | "login"
  | "profile"
  | "policies"
  | "calendar"
  | "hospital"
  | "pharmacy"
  | "prescription"
  | "services"
  | "transactions"
  | "search"
  | "edit"
  | "medicines"
  | "logout"
  | "moderation"
  | "users";
  
const permissionSystem: Permission[] = [
  {
    role: ["guest"],
    action: "read",
    resource: "signup",
  },
  {
    role: ["guest"],
    action: "read",
    resource: "login",
  },
  {
    role: ["user", "employee", "admin"],
    action: "read",
    resource: "profile",
  },
  {
    role: ["user", "employee", "admin"],
    action: "read",
    resource: "logout",
  },
  {
    role: ["user", "employee", "admin"],
    action: "read",
    resource: "policies",
  },
  {
    role: ["user", "employee", "admin"],
    action: "read",
    resource: "calendar",
  },
  {
    role: ["employee", "admin"],
    action: "read",
    resource: "hospital",
  },
  {
    role: ["employee", "admin"],
    action: "read",
    resource: "pharmacy",
  },
  {
    role: ["employee", "admin"],
    action: "read",
    resource: "prescription",
  },
  {
    role: ["guest", "user", "employee", "admin"],
    action: "read",
    resource: "services",
  },
  {
    role: ["employee", "admin"],
    action: "read",
    resource: "transactions",
  },
  {
    role: ["employee", "admin"],
    action: "read",
    resource: "edit",
  },
  {
    role: ["user", "employee", "admin"],
    action: "read",
    resource: "medicines",
  },
  {
    role: ["admin"],
    action: "read",
    resource: "moderation",
  },
  {
    role: ["employee", "admin"],
    action: "read",
    resource: "users",
  },
];

const userRole = ref("guest");

export const setUser = (role: string) => {
  userRole.value = role;
  if (import.meta.client) {
    localStorage.setItem("role", role);
  }
};

export const getUser = () => {
  if (import.meta.client) {
    const storedRole = localStorage.getItem("role");
    if (storedRole) {
      userRole.value = storedRole;
    }
  }
  return userRole;
};

export const useAuth = (
  action: "create" | "read" | "update" | "delete",
  resource: string,
) =>
  computed(() => {
    let found = false;
    for (const currentPermission of permissionSystem) {
      if (
        currentPermission.action === action &&
        currentPermission.resource === resource
      ) {
        found = true;
        for (const role of currentPermission.role) {
          if (role === getUser().value) {
            return true;
          }
        }
      }
    }
    if (!found) {
      //console.log("not found");
      return false;
    }
    //console.log("no permission");
    return false;
  });
