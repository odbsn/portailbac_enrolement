// css.d.ts
declare module '*.css' {
    const content: { [className: string]: string };
    export default content;
}

// Ou pour une importation à effet secondaire (side effect)
declare module '*.css' {
    const content: any;
    export default content;
}
