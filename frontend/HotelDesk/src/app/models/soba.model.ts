import { StatusSobe } from './enums';

export interface Soba {
  id?: number;
  brojSobe: number;
  sprat?: number;
  cenaPoNoci: number;
  status?: StatusSobe;
  tipSobeId: number;
  tipSobe?: string;
  maxGostiju?: number;
  hotelId: number;
}

export interface SobaStatusRequest {
  status: StatusSobe;
}
