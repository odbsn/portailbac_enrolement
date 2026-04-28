declare module "*.scss" {
  const content: { [className: string]: string };
  export default content;
}

// Pour les imports à effet secondaire (side effects)
declare module "../styles/layout/layout.scss";
declare module "../styles/demo/Demos.scss";
