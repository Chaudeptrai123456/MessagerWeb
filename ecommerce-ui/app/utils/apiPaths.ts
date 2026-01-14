export const API_PATHS = {
  AUTH: {
    REGISTER: "/api/auth/register",
    LOGIN: "/login",
    GET_PROFILE: "/api/user/info",
    LOGOUT: "/logout",
  },
  WAREHOUSE: {
    GET_ALL: "/api/owner/warehouse/get",
    ECOMMERCE: "/api/owner/dashboard/ecommerce",
    GET_ALL_PRODUCT: "/api/owner/warehouse/product/get"
  },
  USER: {
    GET_ALL_USERS: "/api/users",
    GET_USER_BY_ID: (userId: string) => `/api/users/${userId}`,
    CREATE_USER: "/api/user",
    UPDATE_USER: (userId: string) => `/api/users/${userId}`,
    DELETE_USER: (userId: string) => `/api/users/${userId}`,
  },

  TASK: {
    GET_DASHBOARD_DATA: "/api/tasks/dashboard-data",
    GET_USER_DASHBOARD_DATA: "/api/tasks/user-dashboard-data",
    GET_ALL_TASK: "/api/tasks",
    GET_TASK_STATUS: (status: string) => `/api/tasks?status=${status}`,
    GET_TASK_BY_ID: (taskId: string) => `/api/tasks/${taskId}`,
    CREATE_TASK: "/api/tasks/create-task",
    UPDATE_TASK: (taskId: string) => `/api/tasks/${taskId}`,
    DELETE_TASK: (taskId: string) => `/api/tasks/${taskId}`,
    UPDATE_TASK_STATUS: (taskId: string) => `/api/tasks/${taskId}/status`,
    UPDATE_TODO_CHECKLIST: (taskId: string) => `/api/tasks/${taskId}/todo`,
    UPLOAD_FILE: (taskId: string) => `/api/tasks/${taskId}/uploadFile`,
  },

  REPORT: {
    EXPORT_TASKS: "/api/report/export/tasks",
    EXPORT_USER: "/api/report/export/users",
  },
};
