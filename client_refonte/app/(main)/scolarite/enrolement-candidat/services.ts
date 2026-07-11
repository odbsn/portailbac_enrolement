// app/scolarite/enrolement-candidat/services.ts
import { CandidatureService as BaseService } from "@/demo/service/CandidatureService";

// Utiliser le type du service existant pour éviter les incompatibilités
type CandidatDTO = Parameters<typeof BaseService.createCandidat>[0];

export const CandidatureService = {
  // CRUD
  createCandidat: (dto: CandidatDTO) => BaseService.createCandidat(dto),
  updateCandidat: (id: string, dto: CandidatDTO) =>
    BaseService.updateCandidat(id, dto),
  deleteCandidat: (id: string, login?: string) =>
    BaseService.deleteCandidat(id, login),

  // Récupération
  getCandidatsByEtablissement: (etabId: string, edition: number) =>
    BaseService.getCandidatsByEtablissement(etabId, edition),
  getCdtsBySerie: (etabId: string, edition: number) =>
    BaseService.getCdtsBySerie(etabId, edition),
  compteFAEBS_: (etabId: string, edition: number) =>
    BaseService.compteFAEBS_(etabId, edition),
  getCentreExamForI: (etabId: string, edition: number) =>
    BaseService.getCentreExamForI(etabId, edition),

  // Vérifications
  checkDoublon: (
    year: number,
    registry: string,
    centre: string,
    session: number,
  ) => BaseService.checkDoublon(year, registry, centre, session),
  checkDoublonTel: (phone: string, session: number) =>
    BaseService.checkDoublonTel(phone, session),
  checkDoublonEmail: (email: string, session: number) =>
    BaseService.checkDoublonEmail(email, session),
  checkRedoublantOrFraude: (tableNum: number, yearBac: number) =>
    BaseService.checkRedoublantOrFraude(tableNum, yearBac),
  checkByEtatCivil: (codeCentre: string, year: number, registry: string) =>
    BaseService.checkByEtatCivil(codeCentre, year, registry),

  // Listes PDF
  getListByEtab: (
    etabId: string,
    session: number,
    name: string,
    login?: string,
    serie?: string,
    tri?: string,
    option?: string,
    start?: string,
    end?: string,
    cExam?: string,
  ) =>
    BaseService.getListByEtab(
      etabId,
      session,
      name,
      login,
      serie,
      tri,
      option,
      start,
      end,
      cExam,
    ),
  getListOLByEtab: (
    etabId: string,
    session: number,
    name: string,
    login?: string,
    serie?: string,
  ) => BaseService.getListOLByEtab(etabId, session, name, login, serie),
  getListContactsByEtab: (
    etabId: string,
    session: number,
    name: string,
    login?: string,
  ) => BaseService.getListContactsByEtab(etabId, session, name, login),
  getListRejetByEtab: (
    etabId: string,
    session: number,
    name: string,
    login?: string,
  ) => BaseService.getListRejetByEtab(etabId, session, name, login),

  // Données statiques
  getLastProg: () => BaseService.getLastProg(),
  getSeries: () => BaseService.getSeries(),
  getCentreEtatCivils: () => BaseService.getCentreEtatCivils(),
  getCentreExamen: () => BaseService.getCentreExamen(),
  getPays: () => BaseService.getPays(),
  getMatiereOptions: () => BaseService.getMatiereOptions(),
};