import {IActionItem} from '../../core/actions/action-item.interface';
import {IAbstractScreen} from '../../core/interfaces/abstract-screen.interface';

export interface DigitalReadingInterface  extends IAbstractScreen{
    message: string;
    icon: string;
    value: string;
    instructions: string;
    button: IActionItem;
}