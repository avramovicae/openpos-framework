import { IMenuItem } from './../../common/imenuitem';
export interface ISellTemplate {
    scan: IScan;
    statusBar: IStatusBar;
}

export interface IScan {
     scanMinLength: number;
     scanMaxLength: number;
     scanType: string;
     scanActionName: string;
     scanSomethingText: string;
     autoFocusOnScan: boolean;
}

export interface IStatusBar {
    backButton: IMenuItem;
    operatorText: String;
    logoutButton: IMenuItem;
    workstationId: String;
    showScan: boolean;
    showAdmin: boolean;
    showSkip: boolean;
}
