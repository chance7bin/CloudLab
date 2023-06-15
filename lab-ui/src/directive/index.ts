import {hasRole, hasPermi} from "./permission";

import type { App } from "@vue/runtime-core";

export default function directive(app: App) {
    app.directive("hasRole", hasRole);
    app.directive("hasPermi", hasPermi);
}
