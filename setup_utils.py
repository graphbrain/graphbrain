from pathlib import Path
from itertools import chain
from setuptools.extension import Extension


def get_source_file_paths(module_name: str, use_cython: bool) -> list[Path]:
    module_base_path = Path(module_name.replace('.', '/'))
    module_source_file = Path(f"{module_base_path}.{'pyx' if use_cython else 'c'}")
    if module_source_file.exists():
        return [module_source_file]
    return list(Path(f"{module_base_path}").glob(f"**/*.{'pyx' if use_cython else 'c'}"))


def get_sub_module_names(source_file_paths: list[Path]) -> list[str]:
    return [
        str(source_file_path.with_suffix('')).replace("/", ".")
        for source_file_path in source_file_paths
    ]


def get_ext_modules(module_names: list[str], use_cython: bool) -> list[Extension]:
    source_file_paths = list(chain(*[
        get_source_file_paths(module_name, use_cython) for module_name in module_names
    ]))
    sub_module_names = get_sub_module_names(source_file_paths)
    include_dirs = ['.'] if not use_cython else None

    return [
        Extension(module_name, [str(source_file_path)], include_dirs=include_dirs)
        for module_name, source_file_path in zip(sub_module_names, source_file_paths)
    ]
