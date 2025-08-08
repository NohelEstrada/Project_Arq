import axios from "axios";

interface Change {
  fieldNames: string[];
  oldValue: object;
  newValue: object;
  path: string;
}
export const useChanges: () => Ref<Change[]> = () =>
  useState("changes", () => []);

export const addChange = (
  fieldNames: string[],
  oldValue: object,
  newValue: object,
  path: string,
): void => {
  useChanges().value.push({ fieldNames, oldValue, newValue, path });
};

export const applyChange = async (index: number) => {
  const config = useRuntimeConfig();
  const ip = config.public.ip;
  await new Promise((resolve, reject) => {
    notify({
      title: "Applying changes",
      description: "Applying changes to the database",
      type: "loading",
    });
    console.log(
      "puto:" + `http://${ip}:8080/api/${useChanges().value[index].path}`,
    );
    axios
      .put(
        `http://${ip}:8080/api/${useChanges().value[index].path}`,
        useChanges().value[index].newValue,
      )
      .then((res) => {
        notify({
          title: "Changes applied",
          description: "Changes applied to the database",
          type: "success",
        });
        //console.log(res);
      })
      .catch((err) => {
        notify({
          title: "Changes not applied",
          description: "Changes not applied to the database",
          type: "error",
        });
        console.log(err);
      });
  });
};
