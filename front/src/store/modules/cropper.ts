import {defineStore} from 'pinia';
import {store} from '../index';

export interface Ratio {
    label: string;
    value: string | number[];
}

export interface CropperState {
    visible: boolean;
    sourceFile: File | null;
    sourceUrl: string | null;
    croppedFile: File | null;
    croppedUrl: string | null;
    ratios: Ratio[] | null;
    resolve: ((file: File | false) => void) | null;
    reject: ((reason?: any) => void) | null;
}

export const useCropperStore = defineStore('cropper', {
    state: (): CropperState => ({
        visible: false,
        sourceFile: null,
        sourceUrl: null,
        croppedFile: null,
        croppedUrl: null,
        ratios: null,
        resolve: null,
        reject: null,
    }),
    actions: {
        open(input: any, ratios: Ratio[] | null = null): Promise<File | false> {
            const raw: File | undefined =
                input instanceof File
                    ? input
                    : input?.raw instanceof File
                        ? input.raw
                        : input?.originFile instanceof File
                            ? input.originFile
                            : undefined;

            if (!raw) {
                console.error('Invalid upload file: expect File/Blob');
                return Promise.resolve(false);
            }

            if (this.sourceUrl) {
                URL.revokeObjectURL(this.sourceUrl);
            }
            this.sourceFile = raw;
            this.sourceUrl = URL.createObjectURL(raw);
            this.visible = true;
            this.ratios = ratios;

            return new Promise<File | false>((resolve, reject) => {
                this.resolve = resolve;
                this.reject = reject;
            });
        },
        confirm(blob: Blob) {
            const originalName = this.sourceFile?.name || 'image.png';
            const ext = originalName.includes('.') ? (originalName.split('.').pop() as string) : 'png';
            const baseName = originalName.replace(/\.[^/.]+$/, '');
            const type = blob.type || this.sourceFile?.type || 'image/png';
            const file = new File([blob], `${baseName}-cropped.${ext}`, {type});

            // 存储裁剪后的图片
            this.croppedFile = file;
            if (this.croppedUrl) {
                URL.revokeObjectURL(this.croppedUrl);
            }
            this.croppedUrl = URL.createObjectURL(file);

            this.resolve?.(file);
            this.cleanup();
        },
        cancel() {
            this.resolve?.(false);
            this.cleanup();
        },
        cleanup() {
            this.visible = false;
            if (this.sourceUrl) {
                URL.revokeObjectURL(this.sourceUrl);
            }
            this.sourceFile = null;
            this.sourceUrl = null;
            this.ratios = null;
            this.resolve = null;
            this.reject = null;
        },

        // 获取裁剪后的图片
        getCroppedFile(): File | null {
            return this.croppedFile;
        },

        // 清除裁剪后的图片
        clearCroppedFile() {
            if (this.croppedUrl) {
                URL.revokeObjectURL(this.croppedUrl);
            }
            this.croppedFile = null;
            this.croppedUrl = null;
        },

    },
    persist: false
});

export function getCropperStore() {
    return useCropperStore(store);
}