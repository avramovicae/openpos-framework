import { SessionService } from './../../core/services/session.service';
import { Component } from '@angular/core';
import { ILoadingDialogScreen } from './loading-dialog-screen.interface';
import { PosScreen } from '../pos-screen/pos-screen.component';

@Component({
    selector: 'app-loading-dialog',
    templateUrl: './loading-dialog.component.html'
})
export class LoadingDialogComponent extends PosScreen<ILoadingDialogScreen> {

    screen: ILoadingDialogScreen;

    constructor(public session: SessionService) {
        super();
    }

    buildScreen(): void {
    }
}
