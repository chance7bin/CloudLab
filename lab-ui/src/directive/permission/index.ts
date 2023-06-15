import type {Directive, DirectiveBinding} from "vue";
import useUserStore from "@/stores/modules/user";

/**
 * 操作权限处理
 */
export const hasPermi: Directive = {
    mounted(el: HTMLElement, binding: DirectiveBinding) {
        const {value} = binding;
        const all_permission = "*:*:*";
        const store = useUserStore();
        const permissions = store.permissions;
        if (value && value instanceof Array && value.length > 0) {
            const permissionFlag = value;

            const hasPermissions = permissions.some((permission: string) => {
                return all_permission === permission || permissionFlag.includes(permission);
            });

            if (!hasPermissions) {
                el.parentNode && el.parentNode.removeChild(el);
            }
        } else {
            throw new Error(`请设置操作权限标签值`);
        }
    }
};

/**
 * 角色权限处理
 */
export const hasRole: Directive = {
    mounted(el: HTMLElement, binding: DirectiveBinding) {
        const {value} = binding;
        const super_admin = "admin";
        const store = useUserStore();
        const roles = store.roles;

        if (value && value instanceof Array && value.length > 0) {
            const roleFlag = value;

            const hasRole = roles.some((role: string) => {
                return super_admin === role || roleFlag.includes(role);
            });

            if (!hasRole) {
                el.parentNode && el.parentNode.removeChild(el);
            }
        } else {
            throw new Error(`请设置角色权限标签值"`);
        }
    }
};
