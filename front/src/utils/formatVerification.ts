/**
 * 校验手机号格式（+86）
 * @param phone 手机号
 * @returns 是否符合格式要求
 */
export const validatePhone = (phone: string): boolean => {
  const phoneRegex = /^\d{11}$/;
  return phoneRegex.test(phone);
};

/**
 * 校验邮箱格式（以.com结尾）
 * @param email 邮箱地址
 * @returns 是否符合格式要求
 */
export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.com$/;
  return emailRegex.test(email);
};

/**
 * 校验验证码格式（6位数字）
 * @param code 验证码
 * @returns 是否符合格式要求
 */
export const validateCode = (code: string): boolean => {
  const codeRegex = /^\d{6}$/;
  return codeRegex.test(code);
};