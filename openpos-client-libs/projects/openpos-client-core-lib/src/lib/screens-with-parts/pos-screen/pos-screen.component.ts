import {filter, map} from 'rxjs/operators';
import {IAbstractScreen} from '../../core/interfaces/abstract-screen.interface';
import {OpenposMessage} from '../../core/messages/message';
import {MessageTypes} from '../../core/messages/message-types';
import {ScreenValueUpdateMessage} from '../../core/messages/screen-value-update-message';
import {SessionService} from '../../core/services/session.service';
import {IScreen} from '../../shared/components/dynamic-screen/screen.interface';
import {deepAssign} from '../../utilites/deep-assign';
import {IActionItem} from '../../core/actions/action-item.interface';
import {Injector, OnDestroy, Optional} from '@angular/core';
import {ActionService} from '../../core/actions/action.service';
import {Observable, Subject, Subscription} from 'rxjs';

export abstract class PosScreen<T extends IAbstractScreen> implements IScreen, OnDestroy {
    screen: T;
    actionService: ActionService;
    sessionService: SessionService;

    subscriptions = new Subscription();
    destroyed$ = new Subject();

    // I don't completely understand why we need @Optional here. I suspect it has something to do with
    // creating these components dynamically and this being an abstract class.
    constructor( @Optional() injector: Injector) {
        // This should never happen, but just incase lets make sure its not null or undefined
        if ( !!injector ) {
            this.actionService = injector.get(ActionService);
            this.sessionService = injector.get(SessionService);
        }
    }

    show(screen: any) {
        this.screen = deepAssign(this.screen, screen);
        this.buildScreen();
    }

    doAction( action: IActionItem | string, payload?: any) {
        if ( typeof(action) === 'string' ) {
            this.actionService.doAction( {action}, payload);
        } else {
            this.actionService.doAction(action, payload);
        }
    }

    getValueUpdates<T>(path: string): Observable<T>{
        return this.sessionService.getMessages(MessageTypes.SCREEN_VALUE_UPDATE).pipe(
            filter( m => (m as ScreenValueUpdateMessage<T>).valuePath === path ),
            map( m => (m as ScreenValueUpdateMessage<T>).value)
        );
    }

    ngOnDestroy(): void {
        if ( this.subscriptions ) {
            this.subscriptions.unsubscribe();
        }
        this.destroyed$.next();
    }

    abstract buildScreen();
}
